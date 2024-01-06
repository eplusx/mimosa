package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeterStatusResponse(
    val statusCode: Int,
    val message: String,
    val body: MeterStatus,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(MeterStatusResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class MeterStatus(
    val deviceId: String,
    val deviceType: String,
    val hubDeviceId: String,
    val version: String,
    val temperature: Float,
    val humidity: Int,
    val battery: Int,
)