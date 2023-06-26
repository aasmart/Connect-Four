package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.PlayerConnection
import dev.aasmart.models.PlayerSession
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.gameSocket() {
    authenticate("auth-session") {
        webSocket {
            val session = call.sessions.get<PlayerSession>()
            val gameId = call.parameters["game-id"]?.toInt()

            if (gameId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid ID"))
                return@webSocket
            }

            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            // Connection stuff
            val connection = PlayerConnection(this, session.userId)
            GamesFacade.facade
                .get(gameId)
                ?.let { ConnectFourGame(it) }
                ?.addConnection(connection) ?: run {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "No such game"))
                    return@webSocket
                }

            try {
                connection.session.send("Connected to game $gameId")

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Session ${connection.session} disconnected")
                GamesFacade.facade
                    .get(gameId)
                    ?.let { ConnectFourGame(it) }
                    ?.removeConnection(connection)
            }
        }
    }
}