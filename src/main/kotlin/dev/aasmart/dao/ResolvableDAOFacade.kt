package dev.aasmart.dao

import org.jetbrains.exposed.sql.ResultRow

interface ResolvableDAOFacade<T> {
    fun resolveResultRow(row: ResultRow): T
}