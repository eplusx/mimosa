package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hub2StatusResponse(
    val statusCode: Int,
    val message: String,
    val body: Hub2Status,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(Hub2StatusResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class Hub2Status(
    val deviceId: String,
    val deviceType: String,
    val hubDeviceId: String,
    val version: String,
    val temperature: Double,
    val humidity: Int,
    val lightLevel: Int,
)