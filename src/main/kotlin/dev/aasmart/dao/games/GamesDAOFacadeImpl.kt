package dev.aasmart.dao.games

import dev.aasmart.dao.DatabaseFactory.dbQuery
import dev.aasmart.models.Game
import dev.aasmart.models.Games
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*

class GamesDAOFacadeImpl : GamesDAOFacade {
    override fun resultRowToObject(row: ResultRow) = Game(
        id = row[Games.id],
        playerOneId = row[Games.playerOneId],
        playerTwoId = row[Games.playerTwoId]
    )

    override suspend fun createGame(playerOneId: String, playerTwoId: String): Game? = dbQuery {
        val insert = Games.insert {
            it[Games.playerOneId] = playerOneId
            it[Games.playerTwoId] = playerTwoId
        }

        insert.resultedValues?.singleOrNull()?.let(::resultRowToObject)
    }

    override suspend fun editGame(gameId: Int, playerOneId: String, playerTwoId: String): Boolean = dbQuery {
        Games.update(where = { Games.id eq gameId }) {
            it[Games.playerOneId] = playerOneId
            it[Games.playerTwoId] = playerTwoId
        } > 0
    }

    override suspend fun getGame(gameId: Int): Game? = dbQuery{
        Games
            .select { Games.id eq gameId }
            .map(::resultRowToObject)
            .singleOrNull()
    }

    override suspend fun allGames(): List<Game> = dbQuery {
        Games
            .selectAll()
            .map(::resultRowToObject)
    }

    override suspend fun deleteGame(gameId: Int): Game? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGames(): Boolean = dbQuery {
        Games.deleteAll() > 0
    }
}

val gamesFacade: GamesDAOFacadeImpl = GamesDAOFacadeImpl().apply {
    runBlocking {
        deleteGames()
    }
}