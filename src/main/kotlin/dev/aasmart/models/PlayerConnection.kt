package dev.aasmart.models

import io.ktor.websocket.*

class PlayerConnection(val session: DefaultWebSocketSession, val playerId: String) {
}