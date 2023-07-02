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
        route("/api") {
            login()
            game()

            get {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
