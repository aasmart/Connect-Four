package dev.aasmart.routing.games

import io.ktor.server.routing.*

fun Route.game() {
    route("/game") {
        newGame()
        joinGame()
        getPlayerData()
    }
}