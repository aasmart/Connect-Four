package dev.aasmart.routing.games

import dev.aasmart.dao.games.gamesFacade
import dev.aasmart.models.PlayerSession
import dev.aasmart.models.gamesCacheMap
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.playPiece() {
    authenticate("auth-session") {
        post("/play-piece/{index}") {
            val index = call.parameters["index"]?.toInt()
            val gameId = call.parameters["game-id"]?.toInt()

            if(index == null) {
                call.respond(HttpStatusCode.Conflict, "Encountered an error getting the place index")
                return@post
            } else if(gameId == null) {
                call.respond(HttpStatusCode.Conflict, "Encountered an error getting the game id")
                return@post
            }

            val cachedGame = gamesCacheMap[gameId]
            val game = gamesFacade.getGame(gameId)
            if(cachedGame == null || game == null) {
                call.respond(HttpStatusCode.Conflict, "Game does not exist")
                return@post
            }

            val playerSession = call.sessions.get<PlayerSession>()
            if(playerSession == null) {
                call.respond(HttpStatusCode.BadRequest, "No session")
                return@post
            }

            val playerId = playerSession.userId

            // Check if it's not the player's turn
            if(!cachedGame.getIsPlayerOneTurn() && playerId == game.playerOneId ||
                cachedGame.getIsPlayerOneTurn() && playerId == game.playerTwoId
            ) {
                call.respond(HttpStatusCode.Forbidden, "You cannot play a piece")
                return@post
            }

            if(cachedGame.playRound(index))
                call.respond(HttpStatusCode.OK, "Played piece at index $index")
            else
                call.respond(HttpStatusCode.BadRequest, "Can not play the piece at $index")
        }
    }

}