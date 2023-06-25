package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.models.PlayerConnection
import dev.aasmart.models.PlayerSession
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.gameSocket(gamesFacade: GamesDAOFacade) {
    authenticate("auth-session") {
        webSocket {
            val session = call.sessions.get<PlayerSession>()
            val gameId = call.parameters["game-id"]?.toInt()

            if (gameId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid ID"))
                return@webSocket
            }

            val game = gamesFacade.get(gameId)
            if (game == null) {
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "No such game"))
                return@webSocket
            }

            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            // Connection stuff
            val connection = PlayerConnection(this, session.userId)
            game.addConnection(connection)

            try {
                connection.session.send("Connected to game ${game.id}")

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val text: String = frame.readText()

                    game.broadcastState()
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Session ${connection.session} disconnected")
                game.removeConnection(connection)
            }
        }
    }
}