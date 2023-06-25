package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.models.JoinCodes
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.joinGame(gamesFacade: GamesDAOFacade) {
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
        val game = gameId?.let { id -> gamesFacade.get(id) }
        if(game == null) {
            call.respond(HttpStatusCode.NotFound, "Game does not exist")
            return@get
        }

        if(!game.hasPlayer(playerId))
            gamesFacade.edit(
                gameId = game.id,
                playerOneId = game.playerOneId.ifEmpty { playerId },
                playerTwoId = game.playerTwoId.ifEmpty { playerId },
                isPlayerOneTurn = game.getIsPlayerOneTurn(),
                gameStatus = game.getStatus(),
                playerOneRematch = game.getPlayerOneRematch(),
                playerTwoRematch = game.getPlayerTwoRematch()
            )

        call.sessions.set(PlayerSession(
            userId = playerId,
            gameId = game.id
        ))

        call.respondRedirect("/game/${game.id}")
    }
}