package dev.aasmart.dao.games

import dev.aasmart.dao.ResolvableDAOFacade
import dev.aasmart.dao.DatabaseFactory.dbQuery
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.Game
import dev.aasmart.models.GameStatus
import dev.aasmart.models.Games
import dev.aasmart.models.PieceType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*

class GamesDAOFacadeImpl : GamesDAOFacade, ResolvableDAOFacade<Game> {
    override fun resolveResultRow(row: ResultRow) = Game(
        id = row[Games.id],
        boardWidth = row[Games.boardWidth],
        boardHeight = row[Games.boardHeight],
        isPlayerOneTurn = row[Games.isPlayerOneTurn],
        gameStatus = row[Games.gameStatus],
        playerOneId = row[Games.playerOneId],
        playerTwoId = row[Games.playerTwoId],
        playerOneRematch = row[Games.playerOneRematch],
        playerTwoRematch = row[Games.playerTwoRematch],
        gameTilesString = row[Games.gamePieces]
    )

    override suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int,
        boardHeight: Int
    ): Game? = dbQuery {
        val insert = Games.insert {
            it[Games.playerOneId] = playerOneId
            it[Games.playerTwoId] = playerTwoId
            it[Games.boardWidth] = boardWidth
            it[Games.boardHeight] = boardHeight
            it[Games.gameStatus] = GameStatus.WAITING_FOR_PLAYERS
            it[Games.isPlayerOneTurn] = true
            it[Games.playerOneRematch] = false
            it[Games.playerTwoRematch] = false
            it[Games.gamePieces] = List(boardWidth * boardHeight) { PieceType.EMPTY }.joinToString("/")
        }

        insert.resultedValues?.singleOrNull()?.let(::resolveResultRow)
    }

    override suspend fun edit(
        gameId: Int,
        playerOneId: String?,
        playerTwoId: String?,
        isPlayerOneTurn: Boolean?,
        gameStatus: GameStatus?,
        playerOneRematch: Boolean?,
        playerTwoRematch: Boolean?,
        gameTiles: Array<PieceType>?
    ): Boolean = dbQuery {
        Games.update(where = { Games.id eq gameId }) { game ->
            playerOneId?.let { game[Games.playerOneId] = it }
            playerTwoId?.let { game[Games.playerTwoId] = it }
            gameStatus?.let { game[Games.gameStatus] = it }
            isPlayerOneTurn?.let { game[Games.isPlayerOneTurn] = it }
            playerOneRematch?.let { game[Games.playerOneRematch] = it }
            playerTwoRematch?.let { game[Games.playerTwoRematch] = it }
            gameTiles?.let { game[Games.gamePieces] = it.joinToString("/") }
        } > 0
    }

    override suspend fun get(gameId: Int): Game? = dbQuery {
        Games
            .select { Games.id eq gameId }
            .map(::resolveResultRow)
            .singleOrNull()
    }

    override suspend fun all(): List<Game> = dbQuery {
        Games
            .selectAll()
            .map(::resolveResultRow)
    }

    override suspend fun delete(gameId: Int): Game? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGames(): Boolean = dbQuery {
        Games.deleteAll() > 0
    }
}