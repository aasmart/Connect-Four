package dev.aasmart.game

import dev.aasmart.models.PieceType
import kotlinx.serialization.Serializable

@Serializable
data class Tile(
    val pieceType: PieceType,
    val canPlace: Boolean,
    val fall: Boolean
)