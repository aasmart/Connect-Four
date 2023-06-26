package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.dao.games.GamesDAOFacadeCacheImpl
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
        }
    }
}