package net.eplusx.mimosa.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
    install(ContentNegotiation) {
        json()
    }

    configureRouting()
}