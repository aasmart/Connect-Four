package dev.aasmart.plugins

import dev.aasmart.dao.games.gamesFacade
import dev.aasmart.game.Packet
import dev.aasmart.models.PlayerSession
import dev.aasmart.models.PlayerConnection
import dev.aasmart.models.gamesCacheMap
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        authenticate("auth-session") {
            webSocket("/api/game/{id}") {
                val session = call.sessions.get<PlayerSession>()
                val gameId = call.parameters["id"]?.toInt()

                if (gameId == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid ID"))
                    return@webSocket
                }

                val game = gamesFacade.getGame(gameId)
                if (game == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "No such game"))
                    return@webSocket
                }

                if (session == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                    return@webSocket
                }

                val currentGame = gamesCacheMap[gameId]
                if(currentGame == null) {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Game does not exist"))
                    return@webSocket
                }

                // Connection stuff
                val connection = PlayerConnection(this, session.userId)
                currentGame.addConnection(game, connection)

                try {
                    connection.session.send("Connected to game ${game.id}")

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val text: String = frame.readText()
                        val packet = Json.decodeFromString<Packet>(text)

                        val playerId = connection.playerId

                        if(currentGame.getIsPlayerOneTurn() && playerId == game.playerOneId ||
                            !currentGame.getIsPlayerOneTurn() && playerId == game.playTwoId)
                            currentGame.playRound(packet.placeIndex)

                        val state = currentGame.collectAsState()
                        currentGame.getConnections().forEach {
                            it.session.send(Json.encodeToString(state))
                        }
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {
                    println("Session ${connection.session} disconnected")
                    currentGame.removeConnection(game, connection)
                }
            }
        }
    }
}