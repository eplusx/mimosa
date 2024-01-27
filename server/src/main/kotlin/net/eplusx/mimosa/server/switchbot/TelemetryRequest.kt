package net.eplusx.mimosa.server.switchbot

import kotlinx.serialization.Serializable

/**
 * Telemetry data from SwitchBot.
 */
@Serializable
data class TelemetryRequest(
    /**
     * Event type. Can be "changeReport".
     */
    val eventType: String,
    /**
     * Event version.
     */
    val eventVersion: String,
    /**
     * Context information.
     */
    val context: TelemetryContext,
)

@Serializable
data class TelemetryContext(
    /**
     * Device type. Can be "WoMeter", "WoPlugJP" and others.
     */
    val deviceType: String,
    /**
     * Device MAC address.
     */
    val deviceMac: String,
    /**
     * Timestamp of the event, in milliseconds since epoch.
     */
    val timeOfSample: Long,
    // Common optional properties.
    /**
     * Battery level in percentage.
     */
    val battery: Int? = null,
    // Meter properties.
    /**
     * Temperature reading. The unit is determined by [scale].
     */
    val temperature: Double? = null,
    /**
     * Humidity reading in relative humidity percentage.
     */
    val humidity: Int? = null,
    /**
     * Temperature unit. Can be "CELSIUS" or "FAHRENHEIT".
     */
    val scale: String? = null,
    // Plug properties.
    /**
     * Power state. Can be "ON" or "OFF".
     */
    val powerState: String? = null,
)