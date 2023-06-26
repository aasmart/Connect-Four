package dev.aasmart.dao.games

import dev.aasmart.dao.ResolvableDAOFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.PieceType

interface TilesDAOFacade {
    suspend fun create(gameId: Int, pieces: Array<PieceType>): Array<PieceType>?
    suspend fun edit(
        gameId: Int,
        tileIndex: Int
    ): Boolean
    suspend fun get(gameId: Int): Array<PieceType>?
    suspend fun all(): HashMap<Int, Array<PieceType>>
    suspend fun delete(gameId: Int): Boolean?
}