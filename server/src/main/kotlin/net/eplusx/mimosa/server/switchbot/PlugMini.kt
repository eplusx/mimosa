package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.common.Attributes

/**
 * Plug metrics.
 *
 * @property deviceId Device ID (MAC address).
 * @property deviceName Device name.
 * @property voltageVolt Voltage in volts.
 * @property currentAmpere Current in amperes.
 */
data class PlugMini(
    val deviceId: String,
    val deviceName: String,
    val voltageVolt: Double,
    val currentAmpere: Double,
    val powerWatt: Double,
) {
    /**
     * Whether the plug is powered on.
     */
    fun powerState(): Boolean {
        return powerWatt > 0
    }

    fun powerOff(): PlugMini {
        return copy(voltageVolt = 0.0, currentAmpere = 0.0, powerWatt = 0.0)
    }

    fun getAttributes(): Attributes =
        Attributes.builder().put("device_id", deviceId).put("device_name", deviceName).build()

    companion object {
        fun isPlug(deviceType: String): Boolean {
            return when (deviceType) {
                "Plug Mini (JP)", "WoPlugJP", "Plug Mini (US)", "WoPlugUS" -> true
                else -> false
            }
        }
    }
}
