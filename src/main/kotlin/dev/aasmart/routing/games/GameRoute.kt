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

            getGame()
        }
    }
}