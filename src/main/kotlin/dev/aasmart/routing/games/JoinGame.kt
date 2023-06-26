package dev.aasmart.routing.games

import dev.aasmart.GamesFacade
import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.JoinCodes
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.joinGame() {
    get("/join") {
        val joinCode = call.request.queryParameters["join-code"]

        val playerId = call.sessions.get<PlayerSession>()?.userId
        if(playerId == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid session")
            return@get
        } else if(joinCode == null || !JoinCodes.codeMap.containsKey(joinCode)) {
            call.respondRedirect("/")
            return@get
        }

        val gameId = JoinCodes.codeMap[joinCode]
        val game = gameId?.let { id -> GamesFacade.facade.get(id) }?.let { ConnectFourGame(it) }
        if(game == null) {
            call.respond(HttpStatusCode.NotFound, "Game does not exist")
            return@get
        }

        if(!game.hasPlayer(playerId))
            GamesFacade.facade.edit(
                gameId = game.id,
                playerOneId = game.playerOneId.ifEmpty { playerId },
                playerTwoId = game.playerTwoId.ifEmpty { playerId },
            )

        call.sessions.set(PlayerSession(
            userId = playerId,
            gameId = game.id
        ))

        call.respondRedirect("/game/${game.id}")
    }
}