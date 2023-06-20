package dev.aasmart.plugins

import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.application.*

fun Application.configureHTTP() {
    install(CORS) {
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
        allowHost("0.0.0.0:3000")
        allowHost("localhost:3000")
    }
}
