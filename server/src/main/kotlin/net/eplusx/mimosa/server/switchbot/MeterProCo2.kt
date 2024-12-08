package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.common.Attributes

/**
 * Meter Pro 2 CO2 metrics.
 *
 * @property deviceId Device ID (MAC address).
 * @property deviceName Device name.
 * @property temperature Temperature reading in Celsius degree.
 * @property humidity Humidity reading in relative humidity in [0, 1] (1 means 100%).
 * @property co2 CO2 reading in ppm.
 * @property battery Battery level in [0, 1].
 */
data class MeterProCo2(
    val deviceId: String,
    val deviceName: String,
    val temperature: Double,
    val humidity: Double,
    val co2: Int,
    val battery: Double,
) {
    fun getAttributes(): Attributes =
        Attributes
            .builder()
            .put("device_id", deviceId)
            .put("device_name", deviceName)
            .build()

    companion object {
        fun isMeterProCo2(deviceType: String): Boolean =
            when (deviceType) {
                "MeterPro(CO2)" -> true
                else -> false
            }
    }
}
