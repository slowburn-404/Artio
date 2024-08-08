package dev.borisochieng.sketchpad.auth.domain.model

data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val imageUrl: String
)
