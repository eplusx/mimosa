import net.eplusx.logger.Secrets
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SwitchBotClient {
    private val httpClient = HttpClient.newBuilder().build()

    fun getDevices(): HttpResponse<String> {
        return httpClient.send(
            getRequest("devices"),
            HttpResponse.BodyHandlers.ofString()
        )
    }

    private fun buildRequest(endpoint: String): HttpRequest.Builder {
        val token = Secrets.SwitchBot.token
        val secret = Secrets.SwitchBot.secret
        val nonce = UUID.randomUUID().toString()
        val time = "" + Instant.now().toEpochMilli()
        val data = token + time + nonce
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val signature = String(Base64.getEncoder().encode(mac.doFinal(data.toByteArray())))
        return HttpRequest.newBuilder()
            .uri(URI("https://api.switch-bot.com/v1.1/$endpoint"))
            .header("Authorization", token)
            .header("sign", signature)
            .header("nonce", nonce)
            .header("t", time)
    }

    private fun getRequest(endpoint: String): HttpRequest = buildRequest(endpoint).GET().build()
}