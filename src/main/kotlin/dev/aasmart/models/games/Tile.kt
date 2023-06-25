package dev.aasmart.models.games

import dev.aasmart.models.PieceType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Tile(
    val pieceType: PieceType,
    val gameId: Int,
    val index: Int
)

object Tiles : Table() {
    val id = integer("id").autoIncrement()
    val gameId = integer("gameId")
    val pieceType = enumeration<PieceType>("pieceType")
    val index = integer("index")
}