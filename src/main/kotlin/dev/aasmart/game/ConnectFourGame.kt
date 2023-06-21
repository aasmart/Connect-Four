package dev.aasmart.game

import dev.aasmart.models.GameStatus
import dev.aasmart.models.PieceType
import dev.aasmart.utils.*
import kotlin.math.floor

class ConnectFourGame(
    private val boardWidth: Int = 7,
    private val boardHeight: Int = 6,
    private var isPlayerOneTurn: Boolean = true,
    private var gameStatus: GameStatus = GameStatus.WAITING_FOR_PLAYERS
) {
    private val gameTiles = MutableList(boardWidth * boardHeight) { PieceType.EMPTY }.toTypedArray()

    fun getIsPlayerOneTurn(): Boolean {
        return isPlayerOneTurn
    }

    fun canPlaceOnTile(index: Int): Boolean {
        return index in 0 until(gameTiles.size) &&
                gameTiles[index] == PieceType.EMPTY &&
                gameStatus == GameStatus.ACTIVE &&
                (index + boardWidth >= gameTiles.size || gameTiles[index + boardWidth] != PieceType.EMPTY) &&
                gameStatus != GameStatus.WAITING_FOR_PLAYERS
    }

    fun getCurrentPlayerPieceType(): PieceType {
        return if (isPlayerOneTurn) PieceType.RED else PieceType.YELLOW
    }

    private fun placePiece(index: Int): Boolean {
        if(!canPlaceOnTile(index))
            return false

        gameTiles[index] = getCurrentPlayerPieceType()
        return true
    }

    private fun checkGameStatus(placeIndex: Int, placedPieceType: PieceType): GameStatus {
        val matrix = gameTiles.asMatrix(rows = boardHeight, columns = boardWidth)

        val placedTileRow = floor(placeIndex.toDouble() / boardWidth).toInt()
        val placedTileColumn = placeIndex % boardWidth

        val pieceMask = Array(4) { placedPieceType }

        if(matrix?.getColumn(placedTileColumn)?.containsSubarray(pieceMask) == true ||
            matrix?.get(placedTileRow)?.containsSubarray(pieceMask)== true ||
            matrix?.getDiagonal(ArrayDiagonalType.MAJOR, placedTileRow, placedTileColumn)?.containsSubarray(pieceMask) == true ||
            matrix?.getDiagonal(ArrayDiagonalType.MINOR, placedTileRow, placedTileColumn)?.containsSubarray(pieceMask) == true
        )
            return if (placedPieceType == PieceType.RED) GameStatus.PLAYER_ONE_WON else GameStatus.PLAYER_TWO_WON

        if(!gameTiles.contains(PieceType.EMPTY))
            return GameStatus.DRAWN

        return GameStatus.ACTIVE
    }

    fun playRound(placeIndex: Int): Boolean {
        if(!placePiece(placeIndex))
            return false

        gameStatus = checkGameStatus(placeIndex, getCurrentPlayerPieceType())
        if(gameStatus == GameStatus.ACTIVE)
            isPlayerOneTurn = !isPlayerOneTurn

        return true
    }

    fun collectAsState(): GameState = GameState(
            gameTiles = gameTiles.mapIndexed { index, piece -> GameTile(piece.int, canPlaceOnTile(index)) }.toTypedArray(),
            gameStatus = gameStatus.intValue,
            isPlayerOneTurn = isPlayerOneTurn,
    )
}