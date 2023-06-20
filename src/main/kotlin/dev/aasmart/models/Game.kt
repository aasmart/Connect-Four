package dev.aasmart.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
enum class PieceType {
    RED,
    YELLOW,
    EMPTY
}

@Serializable
enum class GameState {
    WON,
    DRAWN,
    ACTIVE
}

@Serializable
class GameBoard {
    val matrix: Array<PieceType> = arrayOf();

}

@Serializable
data class Game(
    val id: Int,
    val playerOneId: String,
    val playTwoId: String,
//    val board: GameBoard,
//    val gameState: GameState
) {
    fun isFull(): Boolean {
        return playTwoId.isNotEmpty() && playerOneId.isNotEmpty()
    }
}

object Games : Table() {
    val id = integer("id").autoIncrement()
    val playerOneId = varchar("playerOneId", 64)
    val playerTwoId = varchar("playerTwoId", 64)

//    val gameState = enumeration<GameState>("gameState")
}