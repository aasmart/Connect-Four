package dev.aasmart.game

import dev.aasmart.models.GameStatus
import dev.aasmart.models.PieceType

class ConnectFourGame(
    private val boardWidth: Int = 7,
    private val boardHeight: Int = 6,
    private var isPlayerOneTurn: Boolean = true,
    private var gameStatus: GameStatus = GameStatus.ACTIVE
) {
    private val gameTiles = MutableList(boardWidth * boardHeight) { PieceType.EMPTY }.toTypedArray()

    fun getIsPlayerOneTurn(): Boolean {
        return isPlayerOneTurn
    }

    fun canPlaceOnTile(index: Int): Boolean {
        return index in 0 until(gameTiles.size) &&
                gameTiles[index] == PieceType.EMPTY &&
                gameStatus == GameStatus.ACTIVE &&
                (index + boardWidth >= gameTiles.size || gameTiles[index + boardWidth] != PieceType.EMPTY)
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

    private fun checkGameStatus(): GameStatus {
        return GameStatus.ACTIVE
    }

    fun playRound(placeIndex: Int): Boolean {
        if(!placePiece(placeIndex))
            return false

        gameStatus = checkGameStatus()
        if(gameStatus == GameStatus.ACTIVE)
            isPlayerOneTurn = !isPlayerOneTurn

        return true;
    }

    fun collectAsState(): GameState = GameState(
            gameTiles = gameTiles.mapIndexed { index, piece -> GameTile(piece.int, canPlaceOnTile(index)) }.toTypedArray(),
            gameStatus = gameStatus.intValue,
            isPlayerOneTurn = isPlayerOneTurn,
            isTurn = false
    )
}