package dev.aasmart.plugins

import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.routing.games.game
import dev.aasmart.routing.login.login
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(gamesFacade: GamesDAOFacade) {
    routing {
        staticResources("/static", "assets") {

        }

        route("/api") {
            login()
            game(gamesFacade)
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

                call.respond(FreeMarkerContent("index.ftl", mapOf("state" to gamesFacade.get(id)?.collectAsState())))
            }
        }
    }
}
