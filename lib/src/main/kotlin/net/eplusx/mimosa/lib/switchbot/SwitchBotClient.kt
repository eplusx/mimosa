package net.eplusx.mimosa.lib.switchbot

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SwitchBotClient(
    private val accessToken: String,
    private val secret: String,
    private val endpointPrefix: String = "https://api.switch-bot.com/v1.1/"
) {
    private val httpClient =
        OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(10)).callTimeout(Duration.ofSeconds(30)).build()

    init {
        require(endpointPrefix.endsWith("/")) { "endpointPrefix must end with /" }
    }

    fun getDevices() = DevicesResponse.json.from(get("devices").body!!.source())

    fun getMeterStatus(deviceId: String) =
        MeterStatusResponse.json.from(get("devices/${deviceId}/status").body!!.source())

    fun getPlugMiniStatus(deviceId: String) =
        PlugMiniStatusResponse.json.from(get("devices/${deviceId}/status").body!!.source())

    private fun buildRequest(endpoint: String): Request.Builder {
        val token = accessToken
        val secret = secret
        val nonce = UUID.randomUUID().toString()
        val time = "" + Instant.now().toEpochMilli()
        val data = token + time + nonce
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val signature = String(Base64.getEncoder().encode(mac.doFinal(data.toByteArray())))
        return Request.Builder()
            .url("$endpointPrefix$endpoint")
            .header("Authorization", token)
            .header("sign", signature)
            .header("nonce", nonce)
            .header("t", time)
    }

    private fun getRequest(endpoint: String): Request = buildRequest(endpoint).get().build()

    private fun get(endpoint: String): Response = httpClient.newCall(getRequest(endpoint)).execute().apply {
        if (!isSuccessful) throw IOException("HTTP $code for $endpoint: $message")
    }
}