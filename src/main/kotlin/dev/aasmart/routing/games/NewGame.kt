package dev.aasmart.routing.games

import dev.aasmart.dao.games.GamesFacade
import dev.aasmart.game.ConnectFourGame
import dev.aasmart.models.JoinCodes
import dev.aasmart.models.PlayerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.newGame() {
    val maxJoinCodeReattempts = 5;
    val joinCodeLength = 6;

    post {
        val session = call.sessions.get<PlayerSession>()
        if(session == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid session")
            return@post
        }

        val game = GamesFacade.facade.create(
            playerOneId = session.userId,
            playerTwoId = ""
        )
        if(game == null) {
            call.respond(HttpStatusCode.Conflict, "Couldn't create a new game")
            return@post
        }

        for(i in 0..(maxJoinCodeReattempts)) {
            val code: StringBuilder = StringBuilder()
            for(num in (0 until joinCodeLength))
                code.append((0 until 10).random())

            if(JoinCodes.codeMap.putIfAbsent(code.toString(), game.id) == null)
                break
            else if(i == maxJoinCodeReattempts)
                JoinCodes.codeMap[game.id.toString()] = game.id
        }

        call.sessions.set(PlayerSession(
            userId = session.userId,
            gameId = game.id
        ))

        call.respond(HttpStatusCode.OK, ConnectFourGame(game).toGame())
    }
}