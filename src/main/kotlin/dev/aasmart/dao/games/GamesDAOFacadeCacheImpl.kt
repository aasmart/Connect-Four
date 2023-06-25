package dev.aasmart.dao.games

import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.GameStatus
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
                ConnectFourGame::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .build(true)

    private val gamesCache = cacheManager.getCache("gamesCache", Int::class.javaObjectType, ConnectFourGame::class.java)

    override suspend fun create(
        playerOneId: String,
        playerTwoId: String,
        boardWidth: Int,
        boardHeight: Int
    ): ConnectFourGame? =
        delegate.create(playerOneId, playerTwoId, boardWidth, boardHeight)
            ?.also { game -> gamesCache.put(game.id, game) }

    override suspend fun edit(
        gameId: Int,
        playerOneId: String,
        playerTwoId: String,
        isPlayerOneTurn: Boolean,
        gameStatus: GameStatus,
        playerOneRematch: Boolean,
        playerTwoRematch: Boolean
    ): Boolean {
        get(gameId)?.let {
            gamesCache.put(gameId, ConnectFourGame(
                gameId,
                playerOneId = playerOneId,
                playerTwoId = playerTwoId,
                isPlayerOneTurn = isPlayerOneTurn,
                gameStatus = gameStatus,
                playerOneRematch = playerOneRematch,
                playerTwoRematch = playerTwoRematch,
                boardHeight = it.boardHeight,
                boardWidth = it.boardWidth
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
        )
    }

    override suspend fun get(gameId: Int): ConnectFourGame? =
        gamesCache[gameId]
            ?: delegate.get(gameId)
                .also { game -> gamesCache.put(gameId, game) }

    override suspend fun all(): List<ConnectFourGame> =
        delegate.all()

    override suspend fun delete(gameId: Int): ConnectFourGame? {
        gamesCache.remove(gameId)
        return delegate.delete(gameId)
    }

    override suspend fun deleteGames(): Boolean {
        TODO("Not yet implemented")
    }
}