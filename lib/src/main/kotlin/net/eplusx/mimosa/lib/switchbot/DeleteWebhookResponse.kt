package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteWebhookResponse(
    val statusCode: Int,
    val message: String,
    val body: DeleteWebhookResponseBody,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(DeleteWebhookResponse::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class DeleteWebhookResponseBody(
    val unknown: String? = null,
)