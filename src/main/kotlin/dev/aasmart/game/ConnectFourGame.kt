package dev.aasmart.game

import dev.aasmart.models.*
import dev.aasmart.models.games.GameState
import dev.aasmart.models.games.GameTile
import dev.aasmart.utils.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import kotlin.math.floor

@Serializable
class ConnectFourGame(
    val id: Int,
    val boardWidth: Int,
    val boardHeight: Int,
    private var isPlayerOneTurn: Boolean,
    private var gameStatus: GameStatus,
    var playerOneId: String,
    var playerTwoId: String,
    private var playerOneRematch: Boolean,
    private var playerTwoRematch: Boolean
) {
    @Transient
    private val gameTiles = MutableList(boardWidth * boardHeight) { PieceType.EMPTY }.toTypedArray()

    companion object {
        private val gamePlayerConnections: MutableMap<Int, MutableSet<PlayerConnection>> = Collections.synchronizedMap(
            HashMap()
        )
    }

    fun isFull(): Boolean {
        return playerTwoId.isNotEmpty() && playerOneId.isNotEmpty()
    }

    fun hasPlayer(playerId: String): Boolean {
        return playerOneId == playerId || playerTwoId == playerId
    }

    fun getPlayerRole(playerId: String): GameRole {
        return when (playerId) {
            playerOneId -> GameRole.PLAYER_ONE
            playerTwoId -> GameRole.PLAYER_TWO
            else -> GameRole.SPECTATOR
        }
    }

    suspend fun broadcastState() {
        gamePlayerConnections[id]?.forEach {
            it.session.send(Json.encodeToString(collectAsState()))
        }
    }

    suspend fun addConnection(connection: PlayerConnection) {
        gamePlayerConnections
            .getOrPut(id) { Collections.synchronizedSet(LinkedHashSet()) } += connection

        if(gameStatus == GameStatus.WAITING_FOR_PLAYERS || gameStatus == GameStatus.PLAYER_DISCONNECTED)
            gameStatus = if (hasEnoughPlayers()) GameStatus.ACTIVE else GameStatus.WAITING_FOR_PLAYERS

        broadcastState()
    }

    suspend fun removeConnection(connection: PlayerConnection) {
        gamePlayerConnections[id]?.let {
            it -= connection
        }

        if(gameStatus == GameStatus.ACTIVE)
            gameStatus = if (hasEnoughPlayers()) GameStatus.ACTIVE else GameStatus.PLAYER_DISCONNECTED

        broadcastState()
    }

    fun getConnections(): Set<PlayerConnection> =
        gamePlayerConnections.getOrDefault(id, LinkedHashSet()).toSet()

    private fun hasEnoughPlayers(): Boolean {
        if (gameStatus != GameStatus.ACTIVE &&
            gameStatus != GameStatus.WAITING_FOR_PLAYERS && gameStatus != GameStatus.PLAYER_DISCONNECTED
        )
            return true

        return isFull()
    }

    private fun canPlaceOnTile(index: Int): Boolean {
        return index in 0 until (gameTiles.size) &&
                gameTiles[index] == PieceType.EMPTY &&
                gameStatus == GameStatus.ACTIVE &&
                (index + boardWidth >= gameTiles.size || gameTiles[index + boardWidth] != PieceType.EMPTY)
    }

    private fun getCurrentPlayerPieceType(): PieceType {
        return if (isPlayerOneTurn) PieceType.RED else PieceType.YELLOW
    }

    private fun placePiece(index: Int): Boolean {
        if (!canPlaceOnTile(index))
            return false

        gameTiles[index] = getCurrentPlayerPieceType()
        return true
    }

    private fun checkGameStatus(placeIndex: Int, placedPieceType: PieceType): GameStatus {
        val matrix = gameTiles.asMatrix(rows = boardHeight, columns = boardWidth)

        val placedTileRow = floor(placeIndex.toDouble() / boardWidth).toInt()
        val placedTileColumn = placeIndex % boardWidth

        val pieceMask = Array(4) { placedPieceType }

        if (matrix?.getColumn(placedTileColumn)?.containsSubarray(pieceMask) == true ||
            matrix?.get(placedTileRow)?.containsSubarray(pieceMask) == true ||
            matrix?.getDiagonal(ArrayDiagonalType.MAJOR, placedTileRow, placedTileColumn)
                ?.containsSubarray(pieceMask) == true ||
            matrix?.getDiagonal(ArrayDiagonalType.MINOR, placedTileRow, placedTileColumn)
                ?.containsSubarray(pieceMask) == true
        )
            return if (placedPieceType == PieceType.RED) GameStatus.PLAYER_ONE_WON else GameStatus.PLAYER_TWO_WON

        if (!gameTiles.contains(PieceType.EMPTY))
            return GameStatus.DRAWN

        return GameStatus.ACTIVE
    }

    private suspend fun resetGame() {
        isPlayerOneTurn = true
        gameStatus = GameStatus.ACTIVE
        gameTiles.fill(PieceType.EMPTY)
        playerOneRematch = false
        playerTwoRematch = false

        broadcastState()
    }

    suspend fun requestRematch(playerId: String, cancelRequest: Boolean) {
        playerOneRematch = (playerId == playerOneId || playerOneRematch) && !cancelRequest
        playerTwoRematch = (playerId == playerTwoId || playerTwoRematch) && !cancelRequest

        if(playerOneRematch && playerTwoRematch)
            resetGame()
        else
            broadcastState()
    }

    suspend fun forfeit(playerId: String): Boolean {
        if(gameStatus != GameStatus.ACTIVE)
            return false

        gameStatus = if(playerOneId == playerId) GameStatus.PLAYER_ONE_FORFEIT
        else if(playerTwoId == playerId) GameStatus.PLAYER_TWO_FORFEIT
        else gameStatus

        broadcastState()

        return true
    }

    suspend fun playRound(placeIndex: Int): Boolean {
        if (!placePiece(placeIndex))
            return false

        gameStatus = checkGameStatus(placeIndex, getCurrentPlayerPieceType())
        if (gameStatus == GameStatus.ACTIVE)
            isPlayerOneTurn = !isPlayerOneTurn

        broadcastState()

        return true
    }

    fun getIsPlayerOneTurn(): Boolean {
        return isPlayerOneTurn
    }

    fun getStatus(): GameStatus {
        return gameStatus
    }

    fun getPlayerOneRematch(): Boolean {
        return playerOneRematch
    }

    fun getPlayerTwoRematch(): Boolean {
        return playerTwoRematch
    }

    fun collectAsState(): GameState = GameState(
        gameTiles = gameTiles.mapIndexed { index, piece -> GameTile(piece.int, canPlaceOnTile(index)) }.toTypedArray(),
        gameStatus = gameStatus.ordinal,
        isPlayerOneTurn = isPlayerOneTurn,
        playerOneRematch = playerOneRematch,
        playerTwoRematch = playerTwoRematch,
        joinCode = JoinCodes.codeMap.filterValues { it == id }.keys.first()
    )
}