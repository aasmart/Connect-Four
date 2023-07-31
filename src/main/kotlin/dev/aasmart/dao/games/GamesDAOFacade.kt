package dev.aasmart.dao.games

import dev.aasmart.models.games.Game
import dev.aasmart.models.games.GameStatus
import dev.aasmart.models.games.PieceType

interface GamesDAOFacade {
    suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int = 7,
        boardHeight: Int = 6
    ): Game?
    suspend fun edit(
        gameId: Int,
        playerOneId: String? = null,
        playerTwoId: String? = null,
        isPlayerOneTurn: Boolean? = null,
        gameStatus: GameStatus? = null,
        playerOneRematch: Boolean? = null,
        playerTwoRematch: Boolean? = null,
        gameTiles: Array<PieceType>? = null,
        rematchDenied: Boolean? = null,
        disconnectedPlayerTimeout: String? = null
    ): Boolean
    suspend fun get(gameId: Int): Game?
    suspend fun all(): List<Game>
    suspend fun delete(gameId: Int): Boolean
}