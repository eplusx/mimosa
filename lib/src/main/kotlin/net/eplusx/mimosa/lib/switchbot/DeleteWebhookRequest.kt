package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteWebhookRequest(
    val action: String = "deleteWebhook",
    val url: String,
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(DeleteWebhookRequest::class.java)
    }
}
