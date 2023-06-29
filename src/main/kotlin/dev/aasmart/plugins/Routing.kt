package dev.aasmart.plugins

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.routing.games.game
import dev.aasmart.routing.login.login
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("/static", "assets") {

        }

        route("/api") {
            login()
            game()
        }

        get {
            call.respond(FreeMarkerContent("index.ftl", mapOf<String, String>()))
        }

        authenticate("auth-session") {
            get("/game/{id}") {
                val id = call.parameters["id"]?.toInt()

                if(id == null) {
                    call.respond(HttpStatusCode.NotFound, "Game does not exist")
                    return@get
                }

                call.respond(FreeMarkerContent(
                    "game.ftl",
                    mapOf("state" to GamesFacade.facade.get(id)?.let { ConnectFourGame(it) }?.collectAsState()))
                )
            }
        }
    }
}
