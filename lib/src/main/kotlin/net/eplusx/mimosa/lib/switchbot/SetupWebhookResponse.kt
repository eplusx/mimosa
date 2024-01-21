package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetupWebhookResponse(
    val statusCode: Int,
    val message: String,
    val body: SetupWebhookResponseBody,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(SetupWebhookResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class SetupWebhookResponseBody(
    val unknown: String? = null,
)