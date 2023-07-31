package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.game.JoinCodes
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.spectateGame() {
    post("/spectate") {
        val joinCode = call.request.queryParameters["join-code"]

        val playerId = call.sessions.get<PlayerSession>()?.userId
        if(playerId == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid session")
            return@post
        } else if(joinCode == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid join code")
            return@post
        } else if(!JoinCodes.codeMap.containsKey(joinCode)) {
            call.respond(HttpStatusCode.NotFound, "No game with this join code exists")
            return@post
        }

        val gameId = JoinCodes.codeMap[joinCode]
        val game = gameId?.let { id -> GamesFacade.facade.get(id) }?.let { ConnectFourGame(it) }
        if(game == null) {
            call.respond(HttpStatusCode.NotFound, "Game does not exist")
            return@post
        }

        if(!game.hasPlayerWithId(playerId)) {
            call.respond(HttpStatusCode.OK, game.toGame())
        } else
            call.respond(HttpStatusCode.Conflict, "Could not spectate this game since you have already joined it")
    }
}