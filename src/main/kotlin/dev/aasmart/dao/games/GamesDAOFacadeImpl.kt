package dev.aasmart.dao.games

import dev.aasmart.dao.DatabaseFactory.dbQuery
import dev.aasmart.dao.ResolvableDAOFacade
import org.jetbrains.exposed.sql.ResultRow
import dev.aasmart.models.games.Game
import dev.aasmart.models.games.GameStatus
import dev.aasmart.models.games.Games
import dev.aasmart.models.games.PieceType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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
        gameTilesString = row[Games.gamePieces],
        rematchDenied = row[Games.rematchDenied]
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
            it[Games.rematchDenied] = false
            it[Games.disconnectedPlayerTimeout] = ""
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
        gameTiles: Array<PieceType>?,
        rematchDenied: Boolean?
    ): Boolean = dbQuery {
        Games.update(where = { Games.id eq gameId }) { game ->
            playerOneId?.let { game[Games.playerOneId] = it }
            playerTwoId?.let { game[Games.playerTwoId] = it }
            gameStatus?.let { game[Games.gameStatus] = it }
            isPlayerOneTurn?.let { game[Games.isPlayerOneTurn] = it }
            playerOneRematch?.let { game[Games.playerOneRematch] = it }
            playerTwoRematch?.let { game[Games.playerTwoRematch] = it }
            gameTiles?.let { game[Games.gamePieces] = it.joinToString("/") }
            rematchDenied?.let { game[Games.rematchDenied] = it }
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

    override suspend fun delete(gameId: Int): Boolean = dbQuery {
        Games.deleteWhere { Games.id eq gameId } > 0
    }
}

object GamesFacade {
    lateinit var facade: GamesDAOFacade
}