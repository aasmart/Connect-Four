package dev.aasmart.models.games

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

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
    PLAYER_TWO_FORFEIT,
    PLAYERS_DISCONNECTED
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
    val boardWidth: Int,
    val boardHeight: Int,
    var isPlayerOneTurn: Boolean,
    var gameStatus: GameStatus,
    var playerOneId: String,
    var playerTwoId: String,
    var playerOneRematch: Boolean,
    var playerTwoRematch: Boolean,
    val gameTilesString: String,
    val rematchDenied: Boolean,
    val disconnectedPlayerTimeout: String?,
) : java.io.Serializable

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
    val gamePieces = text("gamePieces")
    val rematchDenied = bool("rematchDenied")
    val disconnectedPlayerTimeout = varchar("disconnectedPlayerTimeout", 128)
}