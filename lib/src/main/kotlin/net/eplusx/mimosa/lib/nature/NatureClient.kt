package net.eplusx.mimosa.lib.nature

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Duration

class NatureClient(
    private val accessToken: String,
    private val endpointPrefix: String = "https://api.nature.global/1/",
) {
    private val httpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .callTimeout(Duration.ofSeconds(30))
            .build()

    init {
        require(endpointPrefix.endsWith("/")) { "endpointPrefix must end with /" }
    }

    fun getDevices() = DevicesResponse.fromJson(get("devices").body!!.source())

    private fun buildRequest(endpoint: String): Request.Builder {
        val token = accessToken
        return Request
            .Builder()
            .url("$endpointPrefix$endpoint")
            .header("Authorization", "Bearer $token")
    }

    private fun getRequest(endpoint: String): Request = buildRequest(endpoint).get().build()

    private fun get(endpoint: String): Response =
        httpClient.newCall(getRequest(endpoint)).execute().apply {
            if (!isSuccessful) throw IOException("HTTP $code for $endpoint: $message")
        }
}
