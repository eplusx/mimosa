package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.common.Attributes

/**
 * Hub 2 metrics.
 *
 * @property deviceId Device ID (MAC address).
 * @property deviceName Device name.
 * @property temperature Temperature reading in Celsius degree.
 * @property humidity Humidity reading in relative humidity in [0, 1] (1 means 100%).
 * @property lightLevel Light level in [1, 20].
 */
data class Hub2(
    val deviceId: String,
    val deviceName: String,
    val temperature: Double,
    val humidity: Double,
    val lightLevel: Int,
) {
    fun getAttributes(): Attributes =
        Attributes
            .builder()
            .put("device_id", deviceId)
            .put("device_name", deviceName)
            .build()

    companion object {
        fun isHub2(deviceType: String): Boolean =
            when (deviceType) {
                "Hub 2", "WoHub2" -> true
                else -> false
            }
    }
}
