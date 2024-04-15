package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.OpenTelemetry
import mu.KotlinLogging
import net.eplusx.mimosa.lib.computeVaporPressureDeficit
import net.eplusx.mimosa.lib.computeVolumetricHumidity
import net.eplusx.mimosa.lib.switchbot.SwitchBotClient
import java.time.Duration
import java.util.Timer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.withLock
import io.opentelemetry.api.metrics.Meter as OpenTelemetryMeter

val logger = KotlinLogging.logger { }

class SwitchBotMetrics(
    openTelemetry: OpenTelemetry,
    private val switchBotClient: SwitchBotClient,
    private val maxUpdatesPerDay: Int = 9500,
) {
    private val meter: OpenTelemetryMeter =
        openTelemetry.meterBuilder("mimosa-switchbot").setInstrumentationVersion("0.1.0").build()

    private val meterMap: MutableMap<String, Meter>
    private val hub2Map: MutableMap<String, Hub2>
    private val plugMiniMap: MutableMap<String, PlugMini>
    private val metricsLock = ReentrantLock()

    init {
        // TODO: Consider parallelizing requests to SwitchBot API. It takes ~2 seconds per device, which makes very long to start up the server.
        // TODO: Retry on failure.
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
                plugMiniStatus.powerWatt,
                PlugMini.guessPowerState(plugMiniStatus),
            )
        }.toMutableMap()
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
        meter.gaugeBuilder("vapor_pressure_deficit").setDescription("Vapor pressure deficit").setUnit("kPa").buildWithCallback {
            metricsLock.withLock {
                for (meter in meterMap.values) {
                    it.record(computeVaporPressureDeficit(meter.temperature, meter.humidity), meter.getAttributes())
                }
                for (hub2 in hub2Map.values) {
                    it.record(computeVaporPressureDeficit(hub2.temperature, hub2.humidity), hub2.getAttributes())
                }
            }
        }
        meter.gaugeBuilder("volumetric_humidity").setDescription("Volumetric humidity").setUnit("gm3").buildWithCallback {
            metricsLock.withLock {
                for (meter in meterMap.values) {
                    it.record(computeVolumetricHumidity(meter.temperature, meter.humidity), meter.getAttributes())
                }
                for (hub2 in hub2Map.values) {
                    it.record(computeVolumetricHumidity(hub2.temperature, hub2.humidity), hub2.getAttributes())
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
            metricsLock.withLock {
                for (hub2 in hub2Map.values) {
                    it.record(hub2.lightLevel.toLong(), hub2.getAttributes())
                }
            }
        }
        meter.gaugeBuilder("power").setDescription("Power").setUnit("watt").buildWithCallback {
            metricsLock.withLock {
                for (plugMini in plugMiniMap.values) {
                    it.record(plugMini.powerWatt, plugMini.getAttributes())
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
            if (powerState != plugMini.powerState) {
                logger.info { "Plug Mini update for $deviceId (${plugMini.deviceName}): powerState $powerState" }
                if (powerState) {
                    plugMiniMap[deviceId] = plugMini.powerOn()
                } else {
                    plugMiniMap[deviceId] = plugMini.powerOff()
                }
            }
        }
    }

    fun startUpdater() {
        val numDevicesToUpdate = plugMiniMap.size
        // It's possible that the interval can be shortened if devices are not powered on all the time, and we know the
        // number of requests sent to the SwitchBot server. Maybe it's not worth the effort yet.
        val updateInterval =
            Duration.ofDays(1).dividedBy(maxUpdatesPerDay.toLong()).multipliedBy(numDevicesToUpdate.toLong())
        logger.info { "Found $numDevicesToUpdate devices to update, interval is set to ${updateInterval.toSeconds()} seconds" }
        val timer = Timer("switchbot-updater")
        timer.scheduleAtFixedRate(updateInterval.toMillis(), updateInterval.toMillis()) {
            try {
                // Get the cached copy first; it takes time to get the stats with SwitchBot API, and it takes too much
                // to acquire the loch for the entire update process.
                val plugMinis = metricsLock.withLock { plugMiniMap.values.map { it.copy() } }
                for (plugMini in plugMinis) {
                    if (plugMini.powerState) {
                        val plugMiniStatus = switchBotClient.getPlugMiniStatus(plugMini.deviceId).body
                        metricsLock.withLock {
                            plugMiniMap[plugMini.deviceId] = plugMini.copy(
                                voltageVolt = plugMiniStatus.voltageVolt,
                                powerWatt = plugMiniStatus.powerWatt,
                                // Update the powerState as well; it might be turned off while this device waits its turn.
                                powerState = PlugMini.guessPowerState(plugMiniStatus)
                            )
                        }
                    }
                }
            } catch (t: Throwable) {
                logger.error(t) { "Unhandled throwable in the updater" }
            }
        }
    }
}