package net.eplusx.mimosa.server.switchbot

import io.opentelemetry.api.common.Attributes
import net.eplusx.mimosa.lib.switchbot.PlugMiniStatus

/**
 * Plug metrics.
 *
 * @property deviceId Device ID (MAC address).
 * @property deviceName Device name.
 * @property voltageVolt Voltage in volts.
 * @property powerWatt Power in watts.
 * @property powerState Whether the power is on.
 */
data class PlugMini(
    val deviceId: String,
    val deviceName: String,
    val voltageVolt: Double,
    val powerWatt: Double,
    val powerState: Boolean,
) {
    fun powerOn(): PlugMini {
        return copy(powerState = true)
    }

    fun powerOff(): PlugMini {
        return copy(voltageVolt = 0.0, powerWatt = 0.0, powerState = false)
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

        fun guessPowerState(status: PlugMiniStatus): Boolean {
            // There is no way to know the power state from the status or other information available in SwitchBot API.
            // * There is no field like "powerState" in the status.
            // * Voltage is always ~100 volt even when the power is off.
            // * Power can be 0 watt even when the power is on if nothing plugged to it consumes energy.
            // As the closest approximation, we assume that the power is on when the power is not 0 watt.
            return status.powerWatt > 0.0
        }
    }
}
