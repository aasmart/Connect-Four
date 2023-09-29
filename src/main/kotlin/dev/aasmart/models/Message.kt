package dev.aasmart.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val userId: String,
    val contents: String
)
