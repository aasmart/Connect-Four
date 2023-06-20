package dev.aasmart.models

import io.ktor.server.auth.*

data class PlayerSession(val userId: String, val gameId: Int?) : Principal
