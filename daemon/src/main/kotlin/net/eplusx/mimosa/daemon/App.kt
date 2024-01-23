package net.eplusx.mimosa.daemon

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient

fun main(args: Array<String>) {
    val daemon = MimosaDaemon(
        AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk,
        NatureClient(Secrets.Nature.accessToken),
    )
    daemon.start()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureRouting()
}