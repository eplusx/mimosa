package net.eplusx.mimosa.lib.switchbot

import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger { }

class SwitchBotClient(
    private val accessToken: String,
    private val secret: String,
    private val endpointPrefix: String = "https://api.switch-bot.com/v1.1/",
    private val maxRetries: Int = 10,
    private val retryBaseInterval: Duration = Duration.ofMillis(100),
    httpClient: OkHttpClient? = null,
) {
    private val httpClient: OkHttpClient

    init {
        require(endpointPrefix.endsWith("/")) { "endpointPrefix must end with /" }
        this.httpClient = httpClient ?: OkHttpClient
            .Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .callTimeout(Duration.ofSeconds(30))
            .build()
    }

    fun getDevices() = DevicesResponse.json.from(get("devices").body!!.source())

    fun getMeterStatus(deviceId: String) = MeterStatusResponse.json.from(get("devices/$deviceId/status").body!!.source())

    fun getPlugMiniStatus(deviceId: String) = PlugMiniStatusResponse.json.from(get("devices/$deviceId/status").body!!.source())

    fun getHub2Status(deviceId: String) = Hub2StatusResponse.json.from(get("devices/$deviceId/status").body!!.source())

    fun setupWebhook(url: String) =
        SetupWebhookResponse.json.from(
            post("webhook/setupWebhook", SetupWebhookRequest(url = url).toJson()).body!!.source(),
        )

    fun deleteWebhook(url: String) =
        DeleteWebhookResponse.json.from(
            post("webhook/deleteWebhook", DeleteWebhookRequest(url = url).toJson()).body!!.source(),
        )

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
        return Request
            .Builder()
            .url("$endpointPrefix$endpoint")
            .header("Authorization", token)
            .header("sign", signature)
            .header("nonce", nonce)
            .header("t", time)
    }

    private fun getRequest(endpoint: String): Request = buildRequest(endpoint).get().build()

    private fun postRequest(
        endpoint: String,
        body: String,
    ): Request =
        buildRequest(endpoint)
            .post(
                body.toRequestBody(
                    applicationJsonMediaType,
                ),
            ).build()

    private fun get(endpoint: String): Response =
        retryUntilSuccessful("GET $endpoint") { httpClient.newCall(getRequest(endpoint)).execute() }

    private fun post(
        endpoint: String,
        body: String,
    ): Response = retryUntilSuccessful("POST $endpoint") { httpClient.newCall(postRequest(endpoint, body)).execute() }

    private fun retryUntilSuccessful(
        endpointMessage: String,
        process: () -> Response,
    ): Response {
        var retryInterval = retryBaseInterval
        var retries = 0
        while (true) {
            val response =
                try {
                    process()
                } catch (e: SocketTimeoutException) {
                    // Retry on network errors.
                    null
                }
            if (response != null && response.isSuccessful) {
                if (retries > 0) {
                    logger.debug { "Successful response for $endpointMessage after $retries retries" }
                }
                return response
            }
            if (++retries > maxRetries) throw IOException("$maxRetries retries exhausted for $endpointMessage: ${response?.body}")
            // TODO: Consider factoring out a fake sleeper.
            Thread.sleep(retryBaseInterval.toMillis())
            retryInterval = retryInterval.multipliedBy(2)
        }
    }

    companion object {
        private val applicationJsonMediaType = "application/json; charset=utf-8".toMediaType()
    }
}
