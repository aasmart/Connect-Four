package dev.aasmart.routing.games

import dev.aasmart.models.PlayerSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

val updatePlayerGame = createRouteScopedPlugin("updatePlayerGame") {
    onCall { call ->
        val gameId = call.parameters["game-id"]?.toInt()
        val session = call.sessions.get<PlayerSession>() ?: return@onCall

        call.sessions.set(
            PlayerSession(
                userId = session.userId,
                gameId = gameId
            )
        )
    }
}

fun Route.game() {
    route("/game") {
        newGame()
        joinGame()

        route("/{game-id}") {
            install(updatePlayerGame)

            authenticate("auth-session") {
                getPlayerData()
                rematchRequest()
                playPiece()
                gameSocket()
                forfeit()
                getGame()
            }
        }
    }
}