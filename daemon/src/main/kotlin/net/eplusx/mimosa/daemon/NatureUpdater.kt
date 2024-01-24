package net.eplusx.mimosa.daemon

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.Meter
import net.eplusx.mimosa.lib.nature.NatureClient
import java.time.Duration
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

/**
 * The daemon that periodically retrieves the data from the Nature Remo API and records it to the OpenTelemetry.
 *
 * TODO: Test this class. It is a bit complicated as it should wait for the reader/exporter to retrieve the data.
 *  See https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/telemetry-testing
 */
class NatureUpdater(
    openTelemetry: OpenTelemetry,
    private val natureClient: NatureClient,
    private val updateInterval: Duration = Duration.ofSeconds(60),
) {
    private val meter: Meter = openTelemetry.meterBuilder("mimosa-nature").setInstrumentationVersion("0.1.0").build()

    data class DeviceTemperature(
        val deviceName: String,
        var temperature: Double,
    )

    /**
     * Keeps the mapping from a device ID to the latest temperature.
     *
     * The value of this map is updated by a periodic update task started in [start]. It is read by a callback
     * registered in [registerMetrics].
     */
    private val temperatureMap: Map<String, DeviceTemperature>

    init {
        val devices = natureClient.getDevices()
        temperatureMap = devices.filter { it.newestEvents?.get("te") != null }.associateBy { it.id }
            .mapValues { DeviceTemperature(it.value.name, it.value.newestEvents!!["te"]!!.value.toDouble()) }
        registerMetrics()
    }

    private fun registerMetrics() {
        // Do not set "°C" as the unit; it is automatically appended to the metric name by OpenTelemetry, but "°" is
        // somehow dropped (probably because it's not an ASCII character).
        // Prometheus requires the unit and the metric name suffix to be the same; it causes an error like
        // `msg="Append failed" err="unit \"°C\" not a suffix of metric \"temperature_C\"` in the debug-level log.
        meter.gaugeBuilder("temperature").setDescription("Temperature").setUnit("C").buildWithCallback {
            for (entry in temperatureMap) {
                it.record(
                    entry.value.temperature,
                    Attributes.builder().put("device_id", entry.key).put("device_name", entry.value.deviceName).build()
                )
            }
        }
    }

    fun start() {
        val timer = Timer("nature-updater")
        timer.scheduleAtFixedRate(0, updateInterval.toMillis()) {
            try {
                for (device in natureClient.getDevices()) {
                    device.newestEvents?.get("te")?.let {
                        temperatureMap[device.id]!!.temperature = it.value.toDouble()
                    }
                }
            } catch (t: Throwable) {
                println("Unhandled throwable in the updater: $t")
            }
        }
    }
}