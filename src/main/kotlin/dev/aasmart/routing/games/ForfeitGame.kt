package dev.aasmart.routing.games

import dev.aasmart.dao.games.gamesFacade
import dev.aasmart.models.PlayerSession
import dev.aasmart.models.gamesCacheMap
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.forfeit() {
    post("/forfeit") {
        val gameId = call.parameters["game-id"]?.toInt()
        if(gameId == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid game id")
            return@post
        }

        val game = gamesFacade.getGame(gameId)
        val cachedGame = gamesCacheMap[gameId]
        if(game == null || cachedGame == null) {
            call.respond(HttpStatusCode.NotFound, "Game does not exist")
            return@post
        }

        val playerSession = call.sessions.get<PlayerSession>()
        if(playerSession == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid session")
            return@post
        }

        val playerId = playerSession.userId

        if(cachedGame.forfeit(game, playerId))
            call.respond(HttpStatusCode.OK, "Game forfeited")
        else
            call.respond(HttpStatusCode.Conflict, "Could not forfeit the game")
    }
}