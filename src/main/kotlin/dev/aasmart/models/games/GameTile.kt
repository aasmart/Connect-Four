package dev.aasmart.models.games

import dev.aasmart.models.PieceType
import kotlinx.serialization.Serializable

@Serializable
data class GameTile(
    val pieceType: Int,
    val canPlace: Boolean
)
