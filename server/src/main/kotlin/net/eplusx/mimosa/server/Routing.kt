package net.eplusx.mimosa.server

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import mu.KotlinLogging
import net.eplusx.mimosa.server.switchbot.TelemetryRequest

val logger = KotlinLogging.logger { }

fun Application.configureRouting() {
    routing {
        post("/switchbot/telemetry") {
            val request = call.receive<TelemetryRequest>()
            logger.info { "TelemetryRequest: $request" }
            call.respondText("success")
        }
    }
}