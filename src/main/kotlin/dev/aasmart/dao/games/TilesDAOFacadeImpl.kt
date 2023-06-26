package dev.aasmart.dao.games

import dev.aasmart.dao.DatabaseFactory
import dev.aasmart.dao.DatabaseFactory.dbQuery
import dev.aasmart.dao.ResolvableDAOFacade
import dev.aasmart.models.PieceType
import dev.aasmart.models.games.Tile
import dev.aasmart.models.games.Tiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class TilesDAOFacadeImpl : TilesDAOFacade, ResolvableDAOFacade<Tile> {
    override suspend fun create(gameId: Int, pieces: Array<PieceType>): Array<PieceType>? = dbQuery {
        val batchInsert = Tiles.batchInsert()
    }

    override suspend fun edit(gameId: Int, tileIndex: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun get(gameId: Int): Array<PieceType>? = dbQuery {
        Tiles
            .select { Tiles.gameId eq gameId }
            .map(::resolveResultRow)
    }

    override suspend fun all(): HashMap<Int, Array<PieceType>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(gameId: Int): Boolean? {
        TODO("Not yet implemented")
    }

    override fun resolveResultRow(row: ResultRow) = Tile(
        pieceType = row[Tiles.pieceType],
        gameId = row[Tiles.gameId],
        index = row[Tiles.index]
    )
}