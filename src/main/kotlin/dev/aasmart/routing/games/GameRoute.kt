package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.dao.games.GamesDAOFacadeCacheImpl
import io.ktor.server.routing.*

fun Route.game(gamesFacade: GamesDAOFacade) {
    route("/game") {
        newGame(gamesFacade)
        joinGame(gamesFacade)

        route("/{game-id}") {
            getPlayerData(gamesFacade)
            rematchRequest(gamesFacade)
            playPiece(gamesFacade)
            gameSocket(gamesFacade)
            forfeit(gamesFacade)
        }
    }
}