package dev.aasmart.routing.games

import dev.aasmart.dao.games.gamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.JoinCodes
import dev.aasmart.models.PlayerSession
import dev.aasmart.models.gamesCacheMap
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.newGame() {
    post {
        val session = call.sessions.get<PlayerSession>()
        if(session == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid session")
            return@post
        }

        val game = gamesFacade.createGame(session.userId, "")
        if(game == null) {
            call.respond(HttpStatusCode.Conflict, "Couldn't create a new game")
            return@post
        }

        gamesCacheMap.putIfAbsent(game.id, ConnectFourGame())

        JoinCodes.codeMap[game.id] = game.id

        call.sessions.set(PlayerSession(
            userId = session.userId,
            gameId = game.id
        ))

        call.respondRedirect("/game/${game.id}")
    }
}