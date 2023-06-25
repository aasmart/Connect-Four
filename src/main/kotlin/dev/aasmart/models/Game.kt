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
enum class GameStatus() {
    ACTIVE,
    DRAWN,
    WON,
    PLAYER_ONE_WON,
    PLAYER_TWO_WON,
    WAITING_FOR_PLAYERS,
    PLAYER_DISCONNECTED,
    PLAYER_ONE_FORFEIT,
    PLAYER_TWO_FORFEIT
}

@Serializable
enum class GameRole {
    PLAYER_ONE,
    PLAYER_TWO,
    SPECTATOR
}


@Serializable
data class Game(
    private val gameId: Int,
    private val boardWidth: Int = 7,
    private val boardHeight: Int = 6,
    private var isPlayerOneTurn: Boolean = true,
    private var gameStatus: GameStatus = GameStatus.WAITING_FOR_PLAYERS,
    private var playerOneId: String,
    private var playerTwoId: String,
    private val gameTiles: Array<PieceType>,
    private var playerOneRematch: Boolean = false,
    private var playerTwoRematch: Boolean = false,
)

object Games : Table() {
    val id = integer("id").autoIncrement()
    val playerOneId = varchar("playerOneId", 64)
    val playerTwoId = varchar("playerTwoId", 64)
    val boardWidth = integer("boardWith")
    val boardHeight = integer("boardHeight")
    val isPlayerOneTurn = bool("isPlayerOneTurn")
    val gameStatus = enumeration<GameStatus>("gameStatus")
    val playerOneRematch = bool("playerOneRematch")
    val playerTwoRematch = bool("playerTwoRematch")
}