package net.eplusx.mimosa.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import net.eplusx.mimosa.lib.Secrets
import net.eplusx.mimosa.lib.nature.NatureClient
import net.eplusx.mimosa.lib.switchbot.SwitchBotClient
import net.eplusx.mimosa.server.nature.NatureMetrics
import net.eplusx.mimosa.server.switchbot.SwitchBotMetrics

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk
    val switchBotClient = SwitchBotClient(Secrets.SwitchBot.accessToken, Secrets.SwitchBot.secret)
    val switchBotMetrics = SwitchBotMetrics(openTelemetry, switchBotClient)
    switchBotMetrics.startUpdater()

    val natureUpdater = NatureMetrics(openTelemetry, NatureClient(Secrets.Nature.accessToken))
    natureUpdater.startUpdater()

    install(ContentNegotiation) {
        json()
    }

    configureRouting(switchBotMetrics)
}