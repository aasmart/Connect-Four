package dev.aasmart.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun toServerTime(zonedDateTime: ZonedDateTime): String {
    val dateFormat = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
        .withZone(ZoneId.of("GMT"))
        .withLocale(Locale.US)

    dateFormat.format(zonedDateTime)
    return dateFormat.format(zonedDateTime)
}

fun parseServerToLocalTime(dateString: String): ZonedDateTime {
    val dateFormat = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.US)

    return ZonedDateTime.parse(
        dateString,
        dateFormat
    ).withZoneSameInstant(ZoneId.systemDefault())
}