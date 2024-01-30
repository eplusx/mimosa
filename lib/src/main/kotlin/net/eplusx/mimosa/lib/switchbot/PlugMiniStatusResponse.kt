package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlugMiniStatusResponse(
    val statusCode: Int,
    val message: String,
    val body: PlugMiniStatus,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(PlugMiniStatusResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class PlugMiniStatus(
    val deviceId: String,
    val deviceType: String,
    val hubDeviceId: String,
    val version: String,
    /**
     * Effective voltage at the moment in Volt.
     */
    @Json(name = "voltage") val voltageVolt: Double,
    /**
     * (Effective) current at the moment in milli Ampere. It's said to be in Ampere in the spec, but it's probably wrong.
     */
    @Json(name = "electricCurrent") val currentMilliAmpere: Double,
    /**
     * Power at the moment in Watt. It's said to be the power consumed in a day, but it's probably wrong.
     */
    @Json(name = "weight") val powerWatt: Double,
    /**
     * Something weird. It's said to be the duration the plug is used in a day in minutes, but it's not.
     */
    val electricityOfDay: Int,
)