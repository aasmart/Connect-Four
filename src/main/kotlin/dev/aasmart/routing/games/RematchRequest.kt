package dev.aasmart.routing.games

import dev.aasmart.GamesFacade
import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.rematchRequest() {
    authenticate("auth-session") {
        post("/rematch-request/{action}") {
            var action = call.parameters["action"]?.lowercase()
            if(action == null) {
                action = "send"
            } else if(action != "send" && action != "withdraw") {
                call.respond(HttpStatusCode.BadRequest,
                    "\'$action\' is not an accepted request action. Use either \'send\' or \'withdraw\'"
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
            val cancelRequest = action == "withdraw"

            game.requestRematch(playerId, cancelRequest)
            if(cancelRequest)
                call.respond(HttpStatusCode.OK, "Rematch request withdrawn")
            else
                call.respond(HttpStatusCode.OK, "Rematch request sent")
        }
    }
}