package dev.aasmart.models

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class PlayerSession(
    val userId: String,
    val gameId: Int?
) : Principal
