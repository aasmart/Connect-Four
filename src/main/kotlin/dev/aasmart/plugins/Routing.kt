package dev.aasmart.plugins

import dev.aasmart.routing.games.game
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            game()
            get {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
