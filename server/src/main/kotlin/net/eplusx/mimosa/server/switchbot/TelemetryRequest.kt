package net.eplusx.mimosa.server.switchbot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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
data class TelemetryContext
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
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
        /**
         * Battery level in percentage.
         */
        val battery: Int? = null,
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
        /**
         * CO2 reading in ppm.
         */
        @JsonNames("CO2")
        val co2: Int? = null,
        /**
         * Light level, between 1 and 20.
         */
        val lightLevel: Int? = null,
        /**
         * Power state. Can be "ON" or "OFF".
         */
        val powerState: String? = null,
        /**
         * Detection state. Can be "DETECTED" or "NOT_DETECTED".
         */
        val detectionState: String? = null,
    )
