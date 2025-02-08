package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.common.Attributes

/**
 * Meter metrics.
 *
 * @property deviceId Device ID (MAC address).
 * @property deviceName Device name.
 * @property temperature Temperature reading in Celsius degree.
 * @property humidity Humidity reading in relative humidity in [0, 1] (1 means 100%).
 * @property battery Battery level in [0, 1].
 */
data class Meter(
    val deviceId: String,
    val deviceName: String,
    val temperature: Double,
    val humidity: Double,
    val battery: Double,
) {
    fun getAttributes(): Attributes =
        Attributes
            .builder()
            .put("device_id", deviceId)
            .put("device_name", deviceName)
            .build()

    companion object {
        fun isMeter(deviceType: String): Boolean =
            when (deviceType) {
                "Meter", "MeterPlus", "WoMeter", "WoMeterPlus", "WoIOSensor" -> true
                else -> false
            }
    }
}
