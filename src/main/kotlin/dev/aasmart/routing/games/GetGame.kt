package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getGame() {
    get {
        val gameId = call.parameters["game-id"]?.toInt()
        val game = GamesFacade.facade.get(gameId ?: -1)
        if(game == null) {
            call.respond(HttpStatusCode.NotFound, "Game with ID $gameId does not exist")
            return@get
        }

        call.respond(HttpStatusCode.OK, game)
    }

    get("/state") {
        val gameId = call.parameters["game-id"]?.toInt()
        val game = GamesFacade.facade.get(gameId ?: -1)
        if(game == null) {
            call.respond(HttpStatusCode.NotFound, "Game with ID $gameId does not exist")
            return@get
        }

        val state = ConnectFourGame(game).collectAsState()

        call.respond(HttpStatusCode.OK, state)
    }
}