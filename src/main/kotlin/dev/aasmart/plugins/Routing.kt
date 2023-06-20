package dev.aasmart.plugins

import dev.aasmart.game.Tile
import dev.aasmart.models.PieceType
import dev.aasmart.routing.games.game
import dev.aasmart.routing.login.login
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            login()
            game()
        }

        get {
            call.respond(FreeMarkerContent("login.ftl", mapOf<String, String>()))
        }

        authenticate("auth-session") {
            get("/game/{id}") {
                call.respond(FreeMarkerContent("index.ftl", mapOf("tiles" to MutableList(6 * 7) { Tile(PieceType.EMPTY, true, false) })))
            }
        }
    }
}
