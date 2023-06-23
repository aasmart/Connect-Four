package dev.aasmart.game

import dev.aasmart.models.*
import dev.aasmart.utils.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import kotlin.math.floor

class ConnectFourGame(
    private val boardWidth: Int = 7,
    private val boardHeight: Int = 6,
    private var isPlayerOneTurn: Boolean = true,
    private var gameStatus: GameStatus = GameStatus.WAITING_FOR_PLAYERS,
    private val gameId: Int
) {
    private val gameTiles = MutableList(boardWidth * boardHeight) { PieceType.EMPTY }.toTypedArray()
    private val playerConnections: MutableSet<PlayerConnection> = Collections.synchronizedSet(LinkedHashSet())
    private var playerOneRematch: Boolean = false
    private var playerTwoRematch: Boolean = false

    suspend fun addConnection(game: Game, connection: PlayerConnection) {
        playerConnections += connection
        gameStatus = if (hasEnoughPlayers(game)) GameStatus.ACTIVE else GameStatus.WAITING_FOR_PLAYERS

        playerConnections.forEach {
            it.session.send(Json.encodeToString(collectAsState()))
        }
    }

    suspend fun removeConnection(game: Game, connection: PlayerConnection) {
        playerConnections -= connection
        gameStatus = if (hasEnoughPlayers(game)) GameStatus.ACTIVE else GameStatus.PLAYER_DISCONNECTED

        playerConnections.forEach {
            it.session.send(Json.encodeToString(collectAsState()))
        }
    }

    fun getConnections(): Set<PlayerConnection> = playerConnections.toSet()

    private fun hasEnoughPlayers(game: Game): Boolean {
        if (gameStatus != GameStatus.ACTIVE && gameStatus != GameStatus.WAITING_FOR_PLAYERS && gameStatus != GameStatus.PLAYER_DISCONNECTED)
            return true

        return playerConnections.any { it.playerId == game.playerOneId } &&
                playerConnections.any { it.playerId == game.playerTwoId }
    }

    fun getIsPlayerOneTurn(): Boolean {
        return isPlayerOneTurn
    }

    private fun canPlaceOnTile(index: Int): Boolean {
        return index in 0 until (gameTiles.size) &&
                gameTiles[index] == PieceType.EMPTY &&
                gameStatus == GameStatus.ACTIVE &&
                (index + boardWidth >= gameTiles.size || gameTiles[index + boardWidth] != PieceType.EMPTY) &&
                gameStatus != GameStatus.WAITING_FOR_PLAYERS &&
                gameStatus != GameStatus.PLAYER_DISCONNECTED
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

        playerConnections.forEach {
            it.session.send(Json.encodeToString(collectAsState()))
        }
    }

    suspend fun requestRematch(game: Game, playerId: String, cancelRequest: Boolean) {
        playerOneRematch = (playerId == game.playerOneId || playerOneRematch) && !cancelRequest
        playerTwoRematch = (playerId == game.playerTwoId || playerTwoRematch) && !cancelRequest

        if(playerOneRematch && playerTwoRematch)
            resetGame()
        else {
            playerConnections.forEach {
                it.session.send(Json.encodeToString(collectAsState()))
            }
        }
    }

    suspend fun playRound(placeIndex: Int): Boolean {
        if (!placePiece(placeIndex))
            return false

        gameStatus = checkGameStatus(placeIndex, getCurrentPlayerPieceType())
        if (gameStatus == GameStatus.ACTIVE)
            isPlayerOneTurn = !isPlayerOneTurn

        playerConnections.forEach {
            it.session.send(Json.encodeToString(collectAsState()))
        }

        return true
    }

    fun collectAsState(): GameState = GameState(
        gameTiles = gameTiles.mapIndexed { index, piece -> GameTile(piece.int, canPlaceOnTile(index)) }.toTypedArray(),
        gameStatus = gameStatus.intValue,
        isPlayerOneTurn = isPlayerOneTurn,
        playerOneRematch = playerOneRematch,
        playerTwoRematch = playerTwoRematch,
        joinCode = JoinCodes.codeMap.filterValues { it == gameId }.keys.first()
    )
}