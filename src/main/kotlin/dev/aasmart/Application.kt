package dev.aasmart

import dev.aasmart.dao.DatabaseFactory
import io.ktor.server.application.*
import dev.aasmart.plugins.*
import io.ktor.network.tls.certificates.*
import java.io.File

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    DatabaseFactory.init()

    configureSecurity()
    configureHTTP()
    configureSockets()
    configureSerialization()
    configureRouting()
    configureTemplating()
}

