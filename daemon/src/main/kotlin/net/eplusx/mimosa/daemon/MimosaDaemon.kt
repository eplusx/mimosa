package net.eplusx.mimosa.daemon

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
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
class MimosaDaemon(
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
        // TODO: Consider rewriting with more idiomatic Kotlin code.
        meter.gaugeBuilder("temperature").setDescription("Temperature").setUnit("°C").buildWithCallback {
            for (entry in temperatureMap) {
                val attributes = Attributes.of(
                    AttributeKey.stringKey("device_id"),
                    entry.key,
                    AttributeKey.stringKey("device_name"),
                    entry.value.deviceName,
                )
                it.record(entry.value.temperature, attributes)
            }
        }
    }

    fun start() {
        val timer = Timer("updater")
        timer.scheduleAtFixedRate(0, updateInterval.toMillis()) {
            for (device in natureClient.getDevices()) {
                device.newestEvents?.get("te")?.let {
                    temperatureMap[device.id]!!.temperature = it.value.toDouble()
                }
            }
        }
    }
}