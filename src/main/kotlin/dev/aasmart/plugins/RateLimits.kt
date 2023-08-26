package dev.aasmart.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimits() {
    install(RateLimit) {
        register(RateLimitName("create-game")) {
            rateLimiter(limit = 5, refillPeriod = 30.seconds)
        }
    }
}