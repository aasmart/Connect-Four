package dev.aasmart.plugins

import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.application.*

fun Application.configureHTTP() {
    install(CORS) {
        allowCredentials = true
        allowHost("localhost:3000")
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
    }
}
