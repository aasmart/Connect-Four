package dev.aasmart.dao.games

import org.jetbrains.exposed.sql.ResultRow

interface DAOFacade<T> {
    fun resultRowToObject(row: ResultRow): T
}