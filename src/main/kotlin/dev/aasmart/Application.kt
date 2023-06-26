package dev.aasmart

import dev.aasmart.dao.DatabaseFactory
import dev.aasmart.dao.games.GamesDAOFacade
import dev.aasmart.dao.games.GamesDAOFacadeCacheImpl
import dev.aasmart.dao.games.GamesDAOFacadeImpl
import dev.aasmart.models.Games
import io.ktor.server.application.*
import dev.aasmart.plugins.*
import io.ktor.network.tls.certificates.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import java.io.File

object GamesFacade {
    lateinit var facade: GamesDAOFacade
}

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    DatabaseFactory.init()

    GamesFacade.facade = GamesDAOFacadeCacheImpl(
        GamesDAOFacadeImpl(),
        File(environment.config.property("storage.ehcacheFilePath").getString())
    ).apply {
        runBlocking {
        }
    }

    configureSecurity()
    configureHTTP()
    configureSockets()
    configureSerialization()
    configureRouting()
    configureTemplating()
}

