package dev.aasmart.routing.games

import dev.aasmart.dao.games.gamesFacade
import dev.aasmart.models.PlayerData
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.getPlayerData() {
    authenticate("auth-session") {
        get("/player-data") {
            val gameId = call.parameters["game-id"]?.toInt()

            if (gameId == null) {
                call.respond(HttpStatusCode.Conflict, "Invalid code")
                return@get
            }

            val session = call.sessions.get<PlayerSession>()
            if(session == null) {
                call.respond(HttpStatusCode.Conflict, "Invalid session")
                return@get
            }

            val game = gamesFacade.getGame(gameId = gameId)

            if(game == null) {
                call.respond(HttpStatusCode.NotFound, "The game with id $gameId couldn't be found")
                return@get
            }

            call.respond(HttpStatusCode.OK, PlayerData(
                playerRole = game.getPlayerRole(session.userId)
            ))
        }
    }
}