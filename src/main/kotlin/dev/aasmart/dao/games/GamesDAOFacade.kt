package dev.aasmart.dao.games

import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.GameStatus

interface GamesDAOFacade {
    suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int = 7,
        boardHeight: Int = 6
    ): ConnectFourGame?
    suspend fun edit(
        gameId: Int,
        playerOneId: String,
        playerTwoId: String,
        isPlayerOneTurn: Boolean,
        gameStatus: GameStatus,
        playerOneRematch: Boolean,
        playerTwoRematch: Boolean
    ): Boolean
    suspend fun get(gameId: Int): ConnectFourGame?
    suspend fun all(): List<ConnectFourGame>
    suspend fun delete(gameId: Int): ConnectFourGame?
    suspend fun deleteGames(): Boolean
}