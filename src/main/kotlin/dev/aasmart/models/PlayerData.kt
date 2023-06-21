package dev.aasmart.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val playerRole: GameRole
)
