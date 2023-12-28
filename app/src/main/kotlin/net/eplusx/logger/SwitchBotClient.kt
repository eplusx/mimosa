import net.eplusx.logger.Secrets
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SwitchBotClient {
    private val httpClient =
        OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(10)).callTimeout(Duration.ofSeconds(30)).build()

    fun getDevices(): Response = httpClient.newCall(getRequest("devices")).execute()

    private fun buildRequest(endpoint: String): Request.Builder {
        val token = Secrets.SwitchBot.token
        val secret = Secrets.SwitchBot.secret
        val nonce = UUID.randomUUID().toString()
        val time = "" + Instant.now().toEpochMilli()
        val data = token + time + nonce
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val signature = String(Base64.getEncoder().encode(mac.doFinal(data.toByteArray())))
        return Request.Builder()
            .url("https://api.switch-bot.com/v1.1/$endpoint")
            .header("Authorization", token)
            .header("sign", signature)
            .header("nonce", nonce)
            .header("t", time)
    }

    private fun getRequest(endpoint: String): Request = buildRequest(endpoint).get().build()
}