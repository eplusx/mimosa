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

    private val meterMap: MutableMap<String, Meter>
    private val hub2Map: MutableMap<String, Hub2>
    private val plugMiniMap: MutableMap<String, PlugMini>
    private val metricsLock = ReentrantLock()

    init {
        // TODO: Consider parallelizing requests to SwitchBot API. It takes ~2 seconds per device, which makes very long to start up the server.
        val devices = switchBotClient.getDevices().body.deviceList
        meterMap = devices.filter { Meter.isMeter(it.deviceType) }.associateBy { it.deviceId }.mapValues {
            logger.info { "Found Meter: ${it.value.deviceId} (${it.value.deviceName})" }
            val meterStatus = switchBotClient.getMeterStatus(it.value.deviceId).body
            Meter(
                it.value.deviceId,
                it.value.deviceName,
                meterStatus.temperature,
                0.01 * meterStatus.humidity,
                0.01 * meterStatus.battery,
            )
        }.toMutableMap()
        hub2Map = devices.filter { Hub2.isHub2(it.deviceType) }.associateBy { it.deviceId }.mapValues {
            logger.info { "Found Hub2: ${it.value.deviceId} (${it.value.deviceName})" }
            val hub2Status = switchBotClient.getHub2Status(it.value.deviceId).body
            Hub2(
                it.value.deviceId,
                it.value.deviceName,
                hub2Status.temperature,
                0.01 * hub2Status.humidity,
                hub2Status.lightLevel,
            )
        }.toMutableMap()
        plugMiniMap = devices.filter { PlugMini.isPlug(it.deviceType) }.associateBy { it.deviceId }.mapValues {
            logger.info { "Found Plug Mini: ${it.value.deviceId} (${it.value.deviceName})" }
            val plugMiniStatus = switchBotClient.getPlugMiniStatus(it.value.deviceId).body
            PlugMini(
                it.value.deviceId,
                it.value.deviceName,
                plugMiniStatus.voltageVolt,
                0.001 * plugMiniStatus.currentMilliAmpere,
                plugMiniStatus.powerWatt,
            )
        }.toMutableMap()
        hub2Map
        registerMetrics()
    }

    private fun registerMetrics() {
        meter.gaugeBuilder("temperature").setDescription("Temperature").setUnit("C").buildWithCallback {
            metricsLock.withLock {
                for (meter in meterMap.values) {
                    it.record(meter.temperature, meter.getAttributes())
                }
                for (hub2 in hub2Map.values) {
                    it.record(hub2.temperature, hub2.getAttributes())
                }
            }
        }
        meter.gaugeBuilder("humidity").setDescription("Relative humidity").buildWithCallback {
            metricsLock.withLock {
                for (meter in meterMap.values) {
                    it.record(meter.humidity, meter.getAttributes())
                }
                for (hub2 in hub2Map.values) {
                    it.record(hub2.humidity, hub2.getAttributes())
                }
            }
        }
        meter.gaugeBuilder("battery").setDescription("Battery SoC").buildWithCallback {
            metricsLock.withLock {
                for (meter in meterMap.values) {
                    it.record(meter.battery, meter.getAttributes())
                }
            }
        }
        meter.gaugeBuilder("light_level").ofLongs().setDescription("Light level").buildWithCallback {
            with(metricsLock) {
                for (hub2 in hub2Map.values) {
                    it.record(hub2.lightLevel.toLong(), hub2.getAttributes())
                }
            }
        }
    }

    fun update(request: TelemetryRequest) {
        val context = request.context
        if (Meter.isMeter(context.deviceType) && context.scale == "CELSIUS") {
            updateMeter(context.deviceMac, context.temperature!!, 0.01 * context.humidity!!, 0.01 * context.battery!!)
        } else if (Hub2.isHub2(context.deviceType) && context.scale == "CELSIUS") {
            updateHub2(context.deviceMac, context.temperature!!, 0.01 * context.humidity!!, context.lightLevel!!)
        } else if (PlugMini.isPlug(context.deviceType)) {
            updatePlugMini(context.deviceMac, context.powerState == "ON")
        } else {
            logger.info { "Unhandled TelemetryRequest: $request" }
        }
    }

    private fun updateMeter(deviceId: String, temperature: Double, humidity: Double, battery: Double) {
        metricsLock.withLock {
            val meter = meterMap[deviceId]
            if (meter == null) {
                logger.warn { "Unknown device ID: $deviceId" }
                return
            }
            logger.info { "Meter update for $deviceId (${meter.deviceName}): temperature $temperature, humidity $humidity, battery $battery" }
            meterMap[deviceId] = meter.copy(temperature = temperature, humidity = humidity, battery = battery)
        }
    }

    private fun updateHub2(deviceId: String, temperature: Double, humidity: Double, lightLevel: Int) {
        metricsLock.withLock {
            val hub2 = hub2Map[deviceId]
            if (hub2 == null) {
                logger.warn { "Unknown device ID: $deviceId" }
                return
            }
            logger.info { "Hub2 update for $deviceId (${hub2.deviceName}): temperature $temperature, humidity $humidity, lightLevel $lightLevel" }
            hub2Map[deviceId] = hub2.copy(temperature = temperature, humidity = humidity, lightLevel = lightLevel)
        }
    }

    private fun updatePlugMini(deviceId: String, powerState: Boolean) {
        metricsLock.withLock {
            val plugMini = plugMiniMap[deviceId]
            if (plugMini == null) {
                logger.warn { "Unknown device ID: $deviceId" }
                return
            }
            if (powerState != plugMini.powerState()) {
                logger.info { "Plug Mini update for $deviceId (${plugMini.deviceName}): powerState $powerState" }
                if (powerState) {
                    TODO("Start updating the Plug Mini metrics")
                } else {
                    plugMiniMap[deviceId] = plugMini.powerOff()
                }
            }
        }
    }
}