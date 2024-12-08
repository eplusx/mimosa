package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeterProCo2StatusResponse(
    val statusCode: Int,
    val message: String,
    val body: MeterProCo2Status,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(MeterProCo2StatusResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class MeterProCo2Status(
    val deviceId: String,
    val deviceType: String,
    val hubDeviceId: String,
    val battery: Int,
    val version: String,
    val temperature: Double,
    val humidity: Int,
    @Json(name = "CO2")
    val co2: Int,
)
