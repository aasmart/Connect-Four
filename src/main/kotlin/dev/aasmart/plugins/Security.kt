package dev.aasmart.plugins

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import java.util.*

fun Application.configureSecurity() {
    install(Sessions) {
        val secretSignKey = hex("6819b57a326945c1968f45236589")
        cookie<PlayerSession>("PLAYER_SESSION") {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<PlayerSession>() == null) {
            call.sessions.set(PlayerSession(
                userId = UUID.randomUUID().toString(),
                gameId = null
            ))
        }
    }

    install(Authentication) {
        session<PlayerSession>("auth-session") {
            validate { player ->
                val game = GamesFacade.facade.get(player.gameId ?: -1)?.let { ConnectFourGame(it) } ?: return@validate null

                if(game.playerOneId != player.userId &&
                    game.playerTwoId != player.userId &&
                    game.hasBothPlayers()
                )
                    return@validate null

                player
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized, "You are not able to play in this game.")
            }
        }
    }
}
