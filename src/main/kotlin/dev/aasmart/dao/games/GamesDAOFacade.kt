package dev.aasmart.dao.games

import dev.aasmart.dao.DAOFacade
import dev.aasmart.models.Game

interface GamesDAOFacade : DAOFacade<Game> {
    suspend fun createGame(playerOneId: String, playerTwoId: String): Game?
    suspend fun editGame(gameId: Int, playerOneId: String, playerTwoId: String): Boolean
    suspend fun getGame(gameId: Int): Game?
    suspend fun allGames(): List<Game>
    suspend fun deleteGame(gameId: Int): Game?
    suspend fun deleteGames(): Boolean
}