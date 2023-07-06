package dev.aasmart.game

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.models.*
import dev.aasmart.models.games.GameState
import dev.aasmart.models.games.GameTile
import dev.aasmart.utils.*
import io.ktor.server.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.floor

class ConnectFourGame(
    val id: Int,
    private val boardWidth: Int,
    private val boardHeight: Int,
    private var isPlayerOneTurn: Boolean,
    private var gameStatus: GameStatus,
    var playerOneId: String,
    var playerTwoId: String,
    private var playerOneRematch: Boolean,
    private var playerTwoRematch: Boolean,
    private val gameTiles: Array<PieceType>,
    private var rematchDenied: Boolean,
    private var disconnectedPlayerTimeout: ZonedDateTime?
) {
    constructor(game: Game) : this(
        game.id,
        game.boardWidth,
        game.boardHeight,
        game.isPlayerOneTurn,
        game.gameStatus,
        game.playerOneId,
        game.playerTwoId,
        game.playerOneRematch,
        game.playerTwoRematch,
        game.gameTilesString.split("/").map { PieceType.valueOf(it) }.toTypedArray(),
        game.rematchDenied,
        try { parseServerToLocalTime(game.disconnectedPlayerTimeout ?: "") } catch (e: DateTimeParseException) { null }
    )

    companion object {
        private const val PLAYER_DISCONNECT_MAX_S = 15L

        private val gamePlayerConnections: MutableMap<Int, MutableSet<PlayerConnection>> = Collections.synchronizedMap(
            HashMap()
        )

        private suspend fun handlePlayerDisconnect(gameId: Int, delaySeconds: Long) {
            delay(delaySeconds * 1000)

            val game = GamesFacade.facade.get(gameId)?.let { ConnectFourGame(it) } ?: return

            if(
                game.areBothPlayersConnected() ||
                game.disconnectedPlayerTimeout == null ||
                ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).isBefore(game.disconnectedPlayerTimeout)
            )
                return

            game.gameStatus =
                if(game.hasConnectPlayerWithId(game.playerOneId))
                    GameStatus.PLAYER_ONE_WON
                else
                    GameStatus.PLAYER_TWO_WON

            game.broadcastState()
        }
    }

    fun hasBothPlayers(): Boolean {
        return playerTwoId.isNotEmpty() && playerOneId.isNotEmpty()
    }

    fun hasPlayerWithId(playerId: String): Boolean {
        return playerOneId == playerId || playerTwoId == playerId
    }

    fun getPlayerRole(playerId: String): GameRole {
        return when (playerId) {
            playerOneId -> GameRole.PLAYER_ONE
            playerTwoId -> GameRole.PLAYER_TWO
            else -> GameRole.SPECTATOR
        }
    }

    private fun hasConnectPlayerWithId(playerId: String): Boolean {
        val gameConnections = gamePlayerConnections.getOrDefault(id, LinkedHashSet())
        return gameConnections.any { it.playerId == playerId }
    }

    private fun areBothPlayersConnected(): Boolean {
        return hasConnectPlayerWithId(playerOneId) &&
                hasConnectPlayerWithId(playerTwoId)
    }

    suspend fun broadcastState() {
        GamesFacade.facade.edit(
            id,
            isPlayerOneTurn = isPlayerOneTurn,
            gameStatus = gameStatus,
            playerOneId = playerOneId,
            playerTwoId = playerTwoId,
            playerOneRematch = playerOneRematch,
            playerTwoRematch = playerTwoRematch,
            gameTiles = gameTiles,
            rematchDenied = rematchDenied,
            disconnectedPlayerTimeout = disconnectedPlayerTimeout?.toHttpDateString()
        )

        val state = collectAsState()

        gamePlayerConnections[id]?.forEach {
            it.session.send(Json.encodeToString(state))
        }
    }

    suspend fun addConnection(connection: PlayerConnection) {
        gamePlayerConnections
            .getOrPut(id) { Collections.synchronizedSet(LinkedHashSet()) } += connection

        if(gameStatus == GameStatus.WAITING_FOR_PLAYERS || gameStatus == GameStatus.PLAYER_DISCONNECTED) {
            gameStatus = if (areBothPlayersConnected()) GameStatus.ACTIVE else GameStatus.WAITING_FOR_PLAYERS
            disconnectedPlayerTimeout = null
        }

        broadcastState()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun removeConnection(connection: PlayerConnection) {
        gamePlayerConnections[id]?.let {
            it -= connection
        }

        if(
            (
                gameStatus == GameStatus.ACTIVE ||
                gameStatus == GameStatus.PLAYER_DISCONNECTED ||
                gameStatus == GameStatus.WAITING_FOR_PLAYERS
            ) &&
            !hasConnectPlayerWithId(playerOneId) &&
            !hasConnectPlayerWithId(playerTwoId)
        ) {
            gameStatus = GameStatus.PLAYERS_DISCONNECTED
        } else if(gameStatus == GameStatus.ACTIVE && !areBothPlayersConnected()) {
            gameStatus = GameStatus.PLAYER_DISCONNECTED
            disconnectedPlayerTimeout =
                ZonedDateTime.now().plus(PLAYER_DISCONNECT_MAX_S, ChronoUnit.SECONDS)
            GlobalScope.launch { handlePlayerDisconnect(id, PLAYER_DISCONNECT_MAX_S) }
        }

        broadcastState()
    }

    fun getConnections(): Set<PlayerConnection> =
        gamePlayerConnections.getOrDefault(id, LinkedHashSet()).toSet()

    private fun canPlaceOnTile(index: Int): Boolean {
        return index in 0 until (gameTiles.size) &&
                gameTiles[index] == PieceType.EMPTY &&
                gameStatus == GameStatus.ACTIVE &&
                (index + boardWidth >= gameTiles.size || gameTiles[index + boardWidth] != PieceType.EMPTY)
    }

    private fun getCurrentPlayerPieceType(): PieceType {
        return if (isPlayerOneTurn) PieceType.RED else PieceType.YELLOW
    }

    private fun placePiece(index: Int): Boolean {
        if (!canPlaceOnTile(index))
            return false

        gameTiles[index] = getCurrentPlayerPieceType()
        return true
    }

    private fun checkGameStatus(placeIndex: Int, placedPieceType: PieceType): GameStatus {
        val matrix = gameTiles.asMatrix(rows = boardHeight, columns = boardWidth)

        val placedTileRow = floor(placeIndex.toDouble() / boardWidth).toInt()
        val placedTileColumn = placeIndex % boardWidth

        val pieceMask = Array(4) { placedPieceType }

        if (matrix?.getColumn(placedTileColumn)?.containsSubarray(pieceMask) == true ||
            matrix?.get(placedTileRow)?.containsSubarray(pieceMask) == true ||
            matrix?.getDiagonal(ArrayDiagonalType.MAJOR, placedTileRow, placedTileColumn)
                ?.containsSubarray(pieceMask) == true ||
            matrix?.getDiagonal(ArrayDiagonalType.MINOR, placedTileRow, placedTileColumn)
                ?.containsSubarray(pieceMask) == true
        )
            return if (placedPieceType == PieceType.RED) GameStatus.PLAYER_ONE_WON else GameStatus.PLAYER_TWO_WON

        if (!gameTiles.contains(PieceType.EMPTY))
            return GameStatus.DRAWN

        return GameStatus.ACTIVE
    }

    private suspend fun resetGame() {
        isPlayerOneTurn = true
        gameStatus = GameStatus.ACTIVE
        gameTiles.fill(PieceType.EMPTY)
        playerOneRematch = false
        playerTwoRematch = false
        rematchDenied = false
        disconnectedPlayerTimeout = null

        broadcastState()
    }

    suspend fun requestRematch(playerId: String, type: RematchRequestType): Boolean {
        if(rematchDenied)
            return false

        if(
            ((playerOneRematch && playerId == playerTwoId) || (playerTwoRematch && playerId == playerOneId)) &&
            type == RematchRequestType.REJECT
        )
            rematchDenied = true
        else {
            playerOneRematch = (playerId == playerOneId || playerOneRematch) && type == RematchRequestType.SEND
            playerTwoRematch = (playerId == playerTwoId || playerTwoRematch) && type == RematchRequestType.SEND
        }

        if(playerOneRematch && playerTwoRematch && !rematchDenied)
            resetGame()
        else
            broadcastState()

        return true
    }

    suspend fun forfeit(playerId: String): Boolean {
        if(gameStatus != GameStatus.ACTIVE)
            return false
        if(!areBothPlayersConnected())
            return false

        gameStatus = if(playerOneId == playerId) GameStatus.PLAYER_ONE_FORFEIT
        else if(playerTwoId == playerId) GameStatus.PLAYER_TWO_FORFEIT
        else gameStatus

        broadcastState()

        return true
    }

    suspend fun playRound(placeIndex: Int): Boolean {
        if (!placePiece(placeIndex))
            return false

        gameStatus = checkGameStatus(placeIndex, getCurrentPlayerPieceType())
        if (gameStatus == GameStatus.ACTIVE)
            isPlayerOneTurn = !isPlayerOneTurn

        broadcastState()

        return true
    }

    fun getIsPlayerOneTurn(): Boolean = isPlayerOneTurn

    fun collectAsState(): GameState = GameState(
        gameTiles = gameTiles.mapIndexed { index, piece -> GameTile(piece.int, canPlaceOnTile(index)) }.toTypedArray(),
        gameStatus = gameStatus.ordinal,
        isPlayerOneTurn = isPlayerOneTurn,
        playerOneRematch = playerOneRematch,
        playerTwoRematch = playerTwoRematch,
        joinCode = JoinCodes.codeMap.filterValues { it == id }.keys.firstOrNull() ?: "",
        playerOneConnected = hasConnectPlayerWithId(playerOneId),
        playerTwoConnected = hasConnectPlayerWithId(playerTwoId),
        rematchDenied = rematchDenied,
        disconnectedPlayerTimeout = disconnectedPlayerTimeout?.toHttpDateString()
    )

    fun toGame(): Game = Game(
        id,
        boardWidth,
        boardHeight,
        isPlayerOneTurn,
        gameStatus,
        playerOneId,
        playerTwoId,
        playerOneRematch,
        playerTwoRematch,
        gameTiles.joinToString("/"),
        rematchDenied,
        disconnectedPlayerTimeout?.let { toServerTime(it) }
    )
}
