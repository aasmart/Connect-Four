package dev.aasmart.models

import dev.aasmart.models.games.GameRole
import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val playerRole: GameRole
)
