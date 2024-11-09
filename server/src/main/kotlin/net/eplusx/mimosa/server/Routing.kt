package net.eplusx.mimosa.server

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import net.eplusx.mimosa.server.switchbot.SwitchBotMetrics
import net.eplusx.mimosa.server.switchbot.TelemetryRequest

fun Application.configureRouting(switchBotMetrics: SwitchBotMetrics) {
    routing {
        post("/switchbot/telemetry") {
            val request = call.receive<TelemetryRequest>()
            switchBotMetrics.update(request)
            call.respondText("success")
        }
    }
}
