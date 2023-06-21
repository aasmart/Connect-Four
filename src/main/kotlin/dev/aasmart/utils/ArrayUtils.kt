package dev.aasmart.utils

import java.util.*

inline fun <reified T> Array<T>.asMatrix(rows: Int, columns: Int): Array<Array<T>>? {
    if(this.size > rows * columns)
        return null

    val matrix = mutableListOf<Array<T>>()

    for(i in 0 until rows) {
        matrix.add(this.sliceArray(i * columns until i * columns + columns))
    }

    return matrix.toTypedArray()
}

inline fun <reified T> Array<Array<T>>.getColumn(column: Int): Array<T>? {
    if(column !in 0 until this[0].size)
        return null

    return this.map { it[column] }.toTypedArray()
}

enum class ArrayDiagonalType {
    MAJOR,
    MINOR
}

inline fun <reified T> Array<Array<T>>.getDiagonal(type: ArrayDiagonalType, startRow: Int, startColumn: Int): Array<T>? {
    if(startColumn !in this[0].indices)
        return null

    return this.mapIndexed { index, arr ->
        val distance = (startRow - index) * (if (type == ArrayDiagonalType.MAJOR) 1 else -1)
        val pos = distance + startColumn

        if (pos !in arr.indices)
            return@mapIndexed null

        return@mapIndexed arr[pos]
    }.filterNotNull().toTypedArray()
}

inline fun <reified T> Array<T>.containsSubarray(subArray: Array<T>): Boolean {
    return Collections.indexOfSubList(this.toList(), subArray.toList()) >= 0
}