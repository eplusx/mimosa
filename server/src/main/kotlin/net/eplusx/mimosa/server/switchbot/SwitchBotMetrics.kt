package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.OpenTelemetry
import mu.KotlinLogging
import net.eplusx.mimosa.lib.switchbot.SwitchBotClient
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import io.opentelemetry.api.metrics.Meter as OpenTelemetryMeter

val logger = KotlinLogging.logger { }

class SwitchBotMetrics(
    openTelemetry: OpenTelemetry,
    private val switchBotClient: SwitchBotClient,
) {
    private val meter: OpenTelemetryMeter =
        openTelemetry.meterBuilder("mimosa-switchbot").setInstrumentationVersion("0.1.0").build()

    private val meterMap: Map<String, Meter>
    private val meterLock = ReentrantLock()

    init {
        val devices = switchBotClient.getDevices().body.deviceList
        meterMap = devices.filter { Meter.isMeter(it.deviceType) }.associateBy { it.deviceId }
            .mapValues {
                logger.info { "Found meter: ${it.value.deviceId} (${it.value.deviceName})" }
                val meterStatus = switchBotClient.getMeterStatus(it.value.deviceId).body
                Meter(
                    it.value.deviceId,
                    it.value.deviceName,
                    meterStatus.temperature,
                    0.01 * meterStatus.humidity,
                    0.01 * meterStatus.battery,
                )
            }
        registerMetrics()
    }

    private fun registerMetrics() {
        meter.gaugeBuilder("temperature").setDescription("Temperature").setUnit("C").buildWithCallback {
            for (meter in getMeterValues()) {
                it.record(meter.temperature, meter.getAttributes())
            }
        }
        meter.gaugeBuilder("humidity").setDescription("Relative humidity").buildWithCallback {
            for (meter in getMeterValues()) {
                it.record(meter.humidity, meter.getAttributes())
            }
        }
        meter.gaugeBuilder("battery").setDescription("Battery SoC").buildWithCallback {
            for (meter in getMeterValues()) {
                it.record(meter.battery, meter.getAttributes())
            }
        }
    }

    private fun getMeterValues(): List<Meter> {
        return meterLock.withLock { meterMap.values.map { meter -> meter.copy() } }
    }

    fun update(request: TelemetryRequest) {
        val context = request.context
        if (Meter.isMeter(context.deviceType) && context.scale == "CELSIUS") {
            logger.info { "Meter update for ${context.deviceMac}: temperature ${context.temperature}, humidity ${0.01 * context.humidity!!}, battery ${0.01 * context.battery!!}" }
            updateMeter(
                context.deviceMac,
                context.temperature!!,
                0.01 * context.humidity!!,
                0.01 * context.battery!!
            )
        } else {
            logger.info { "Unhandled TelemetryRequest: $request" }
        }
    }

    private fun updateMeter(deviceId: String, temperature: Double, humidity: Double, battery: Double) {
        meterLock.withLock {
            val meter = meterMap[deviceId]
            if (meter == null) {
                logger.warn { "Unknown device ID: $deviceId" }
                return
            }
            meter.temperature = temperature
            meter.humidity = humidity
            meter.battery = battery
        }
    }
}