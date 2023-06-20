package dev.aasmart.game

import dev.aasmart.models.GameStatus
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gameTiles: Array<GameTile>,
    val isTurn: Boolean,
    val isPlayerOneTurn: Boolean,
    val gameStatus: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        return gameTiles.contentEquals(other.gameTiles)
    }

    override fun hashCode(): Int {
        return gameTiles.contentHashCode()
    }
}
