package dev.aasmart.models.games

import kotlinx.serialization.Serializable

@Serializable
data class GameTile(
    val pieceType: Int,
    val canPlace: Boolean
)
