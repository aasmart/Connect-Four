package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.game() {
    route("/game") {
        newGame()
        joinGame()

        route("/{game-id}") {
            getPlayerData()
            rematchRequest()
            playPiece()
            gameSocket()
            forfeit()

            get {
                val gameId = call.parameters["game-id"]?.toInt()
                val game = GamesFacade.facade.get(gameId ?: -1)
                if(game == null) {
                    call.respond(HttpStatusCode.NotFound, "Games with ID $gameId does not exist")
                    return@get
                }

                call.respond(HttpStatusCode.OK, game)
            }
        }
    }
}