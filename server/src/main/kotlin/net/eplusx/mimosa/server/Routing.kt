package net.eplusx.mimosa.server

import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
)

fun Application.configureRouting() {
    routing {
        post("/") {
            val request = call.receive<Request>()
            call.respondText("Hello World, ${request.name}!")
        }
    }
}