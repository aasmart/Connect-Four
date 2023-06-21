package dev.aasmart.models

import dev.aasmart.game.ConnectFourGame
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.Collections

@Serializable
enum class PieceType(val int: Int) {
    RED(0),
    YELLOW(1),
    EMPTY(2)
}

@Serializable
enum class GameStatus(val intValue: Int) {
    ACTIVE(0),
    DRAWN(1),
    WON(2),
    PLAYER_ONE_WON(3),
    PLAYER_TWO_WON(4),
    WAITING_FOR_PLAYERS(5)
}

@Serializable
enum class GameRole {
    PLAYER_ONE,
    PLAYER_TWO,
    SPECTATOR
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

    fun hasPlayer(playerId: String): Boolean {
        return playerOneId == playerId || playTwoId == playerId
    }

    fun getPlayerRole(playerId: String): GameRole {
        return when (playerId) {
            playerOneId -> GameRole.PLAYER_ONE
            playTwoId -> GameRole.PLAYER_TWO
            else -> GameRole.SPECTATOR
        }
    }
}

val gamesCacheMap: MutableMap<Int, ConnectFourGame> = Collections.synchronizedMap(HashMap())

object Games : Table() {
    val id = integer("id").autoIncrement()
    val playerOneId = varchar("playerOneId", 64)
    val playerTwoId = varchar("playerTwoId", 64)

//    val gameState = enumeration<GameState>("gameState")
}