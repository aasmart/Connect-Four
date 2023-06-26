package dev.aasmart.dao.games

import dev.aasmart.dao.ResolvableDAOFacade
import dev.aasmart.dao.DatabaseFactory.dbQuery
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.GameStatus
import dev.aasmart.models.Games
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*

class GamesDAOFacadeImpl : GamesDAOFacade, ResolvableDAOFacade<ConnectFourGame> {
    override fun resolveResultRow(row: ResultRow) = ConnectFourGame(
        id = row[Games.id],
        boardWidth = row[Games.boardWidth],
        boardHeight = row[Games.boardHeight],
        isPlayerOneTurn = row[Games.isPlayerOneTurn],
        gameStatus = row[Games.gameStatus],
        playerOneId = row[Games.playerOneId],
        playerTwoId = row[Games.playerTwoId],
        playerOneRematch = row[Games.playerOneRematch],
        playerTwoRematch = row[Games.playerTwoRematch]
    )

    override suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int,
        boardHeight: Int
    ): ConnectFourGame? = dbQuery {
        val insert = Games.insert {
            it[Games.playerOneId] = playerOneId
            it[Games.playerTwoId] = playerTwoId
            it[Games.boardWidth] = boardWidth
            it[Games.boardHeight] = boardHeight
            it[Games.gameStatus] = GameStatus.WAITING_FOR_PLAYERS
            it[Games.isPlayerOneTurn] = true
            it[Games.playerOneRematch] = false
            it[Games.playerTwoRematch] = false
        }

        insert.resultedValues?.singleOrNull()?.let(::resolveResultRow)
    }

    override suspend fun edit(
        gameId: Int,
        playerOneId: String,
        playerTwoId: String,
        isPlayerOneTurn: Boolean,
        gameStatus: GameStatus,
        playerOneRematch: Boolean,
        playerTwoRematch: Boolean
    ): Boolean = dbQuery {
        Games.update(where = { Games.id eq gameId }) {
            it[Games.playerOneId] = playerOneId
            it[Games.playerTwoId] = playerTwoId
            it[Games.gameStatus] = gameStatus
            it[Games.isPlayerOneTurn] = isPlayerOneTurn
            it[Games.playerOneRematch] = playerOneRematch
            it[Games.playerTwoRematch] = playerTwoRematch
        } > 0
    }

    override suspend fun get(gameId: Int): ConnectFourGame? = dbQuery {
        Games
            .select { Games.id eq gameId }
            .map(::resolveResultRow)
            .singleOrNull()
    }

    override suspend fun all(): List<ConnectFourGame> = dbQuery {
        Games
            .selectAll()
            .map(::resolveResultRow)
    }

    override suspend fun delete(gameId: Int): ConnectFourGame? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGames(): Boolean = dbQuery {
        Games.deleteAll() > 0
    }
}