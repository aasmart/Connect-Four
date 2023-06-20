package dev.aasmart.game

import dev.aasmart.models.PieceType
import kotlinx.serialization.Serializable

@Serializable
data class Packet(
    val placeIndex: Int
)