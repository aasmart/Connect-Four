package dev.aasmart.dao

import org.jetbrains.exposed.sql.ResultRow

interface DAOFacade<T> {
    fun resultRowToObject(row: ResultRow): T
}