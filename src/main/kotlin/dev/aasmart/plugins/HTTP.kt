package dev.aasmart.plugins

import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.application.*

fun Application.configureHTTP() {
    println("${System.getenv("SITE_URL") ?: "localhost"}:${System.getenv("SITE_PORT") ?: "3000"}")
    install(CORS) {
        allowCredentials = true
        allowHost("${System.getenv("SITE_URL") ?: "localhost"}:${System.getenv("SITE_PORT") ?: "3000"}")
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
    }
}
