package dev.aasmart.dao.games

import dev.aasmart.models.Game
import dev.aasmart.models.GameStatus
import dev.aasmart.models.PieceType
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import java.io.File

class GamesDAOFacadeCacheImpl(
    private val delegate: GamesDAOFacade,
    storagePath: File
) : GamesDAOFacade {
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(storagePath))
        .withCache(
            "gamesCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Int::class.javaObjectType,
                Game::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .build(true)

    private val gamesCache = cacheManager.getCache("gamesCache", Int::class.javaObjectType, Game::class.java)

    override suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int,
        boardHeight: Int
    ): Game? =
        delegate.create(playerOneId, playerTwoId, boardWidth, boardHeight)
            ?.also { game -> gamesCache.put(game.id, game) }

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
    ): Boolean {
        get(gameId)?.let {
            gamesCache.put(gameId, Game(
                gameId,
                playerOneId = playerOneId ?: it.playerOneId,
                playerTwoId = playerTwoId ?: it.playerTwoId,
                isPlayerOneTurn = isPlayerOneTurn ?: it.isPlayerOneTurn,
                gameStatus = gameStatus ?: it.gameStatus,
                playerOneRematch = playerOneRematch ?: it.playerOneRematch,
                playerTwoRematch = playerTwoRematch ?: it.playerTwoRematch,
                boardHeight = it.boardHeight,
                boardWidth = it.boardWidth,
                gameTilesString = gameTiles?.joinToString("/") ?: it.gameTilesString,
                rematchDenied = rematchDenied ?: it.rematchDenied
            ))
        }

        return delegate.edit(
            gameId,
            playerOneId,
            playerTwoId,
            isPlayerOneTurn,
            gameStatus,
            playerOneRematch,
            playerTwoRematch,
            gameTiles,
            rematchDenied
        )
    }

    override suspend fun get(gameId: Int): Game? =
        gamesCache[gameId]
            ?: delegate.get(gameId)
                ?.also { game -> gamesCache.put(gameId, game) }

    override suspend fun all(): List<Game> =
        delegate.all()

    override suspend fun delete(gameId: Int): Boolean {
        gamesCache.remove(gameId)
        return delegate.delete(gameId)
    }
}