package net.eplusx.mimosa.server

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient

fun main(args: Array<String>) {
    val natureUpdater = NatureUpdater(
        AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk,
        NatureClient(Secrets.Nature.accessToken),
    )
    natureUpdater.start()
    EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}