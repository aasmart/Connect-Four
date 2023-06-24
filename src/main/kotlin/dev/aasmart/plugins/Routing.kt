package dev.aasmart.plugins

import dev.aasmart.game.Tile
import dev.aasmart.models.PieceType
import dev.aasmart.models.gamesCacheMap
import dev.aasmart.routing.games.game
import dev.aasmart.routing.login.login
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*

fun Application.configureRouting() {
    routing {
        staticResources("/static", "assets") {

        }

        route("/api") {
            login()
            game()
        }

        get {
            call.respond(FreeMarkerContent("login.ftl", mapOf<String, String>()))
        }

        authenticate("auth-session") {
            get("/game/{id}") {
                val id = call.parameters["id"]?.toInt()

                if(id == null) {
                    call.respond(HttpStatusCode.NotFound, "Game does not exist")
                    return@get
                }

                call.respond(FreeMarkerContent("index.ftl", mapOf("state" to gamesCacheMap[id]?.collectAsState())))
            }
        }
    }
}
