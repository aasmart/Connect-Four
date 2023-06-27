package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.game.RematchRequestType
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.lang.IllegalArgumentException

fun Route.rematchRequest() {
    authenticate("auth-session") {
        post("/rematch-request/{action}") {
            val actionString = call.parameters["action"] ?: "send"
            val action = try {
                RematchRequestType.valueOf(actionString.uppercase())
            } catch(e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "\'$actionString\' is not an accepted request action. Use \'send\', \'withdraw\', or \'reject\'"
                )
                return@post
            }

            val gameId = call.parameters["game-id"]?.toInt()
            if(gameId == null) {
                call.respond(HttpStatusCode.Conflict, "Invalid game id")
                return@post
            }

            val game = GamesFacade.facade.get(gameId)?.let { ConnectFourGame(it) }
            if(game == null) {
                call.respond(HttpStatusCode.NotFound, "Game does not exist")
                return@post
            }

            val playerSession = call.sessions.get<PlayerSession>()
            if(playerSession == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid session")
                return@post
            }

            val playerId = playerSession.userId

            if(game.requestRematch(playerId, action)) {
                call.respond(HttpStatusCode.OK, when(action) {
                    RematchRequestType.SEND -> "Rematch request sent"
                    RematchRequestType.WITHDRAW -> "Rematch request withdrawn"
                    RematchRequestType.REJECT -> "Rematch request rejected"
                })
            } else
                call.respond(HttpStatusCode.Conflict, "Could not process rematch request")

        }
    }
}