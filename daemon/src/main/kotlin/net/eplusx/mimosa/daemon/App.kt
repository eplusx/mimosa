package net.eplusx.mimosa.daemon

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient

fun main(args: Array<String>) {
    val daemon = MimosaDaemon(
        AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk,
        NatureClient(Secrets.Nature.accessToken),
    )
    daemon.start()
    EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}