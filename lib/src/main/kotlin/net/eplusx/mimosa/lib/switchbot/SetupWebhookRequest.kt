package net.eplusx.mimosa.lib.switchbot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetupWebhookRequest(
    val action: String = "setupWebhook",
    val url: String,
    val deviceList: String = "ALL",
) {
    fun toJson(): String = json.to(this)

    companion object {
        val json = JsonSerializer(SetupWebhookRequest::class.java)
    }
}
