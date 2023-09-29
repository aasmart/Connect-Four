package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.Message
import dev.aasmart.models.PlayerConnection
import dev.aasmart.models.PlayerSession
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.gameSocket() {
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

            val game = GamesFacade.facade
                .get(gameId)
                ?.let { ConnectFourGame(it) }

            val player: String = if(game?.playerOneId == session.userId)
                "Player 1"
            else if(game?.playerTwoId == session.userId)
                "Player 2"
            else
                "Spectator"

            game?.getConnections()?.forEach {
                it.session.send(Json.encodeToString(
                    Message("SYSTEM", "$player connected.")
                ))
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val content = frame.readText()
                val message = Json.decodeFromString<Message>(content)

                val updatedMessage: Message = if(game?.playerOneId == session.userId)
                    Message("Player 1", message.contents)
                else if(game?.playerTwoId == session.userId)
                    Message("Player 2", message.contents)
                else
                    Message("Spectator", message.contents)

                game?.getConnections()?.forEach {
                    it.session.send(Json.encodeToString(updatedMessage))
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        } finally {
            val game = GamesFacade.facade
                .get(gameId)
                ?.let { ConnectFourGame(it) }

            val player: String = if(game?.playerOneId == session.userId)
                "Player 1"
            else if(game?.playerTwoId == session.userId)
                "Player 2"
            else
                "Spectator"

            game?.getConnections()?.forEach {
                it.session.send(Json.encodeToString(
                    Message("SYSTEM", "$player disconnected.")
                ))
            }
            println("Session ${connection.session} disconnected")
            game?.removeConnection(connection)
        }
    }
}