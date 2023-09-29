package dev.aasmart.routing.games

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.message() {
    route("/message") {
        post {
            val params = call.receiveParameters()
            val contents = params["contents"]

            call.respondText("Message sent", status = HttpStatusCode.OK)
        }
    }
}