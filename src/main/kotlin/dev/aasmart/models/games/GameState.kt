package dev.aasmart.models.games

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gameTiles: Array<GameTile>,
    val isPlayerOneTurn: Boolean,
    val gameStatus: Int,
    val playerOneRematch: Boolean,
    val playerTwoRematch: Boolean,
    val joinCode: String,
    val playerOneConnected: Boolean,
    val playerTwoConnected: Boolean,
    val rematchDenied: Boolean,
    val playerDisconnectTime: String?
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
