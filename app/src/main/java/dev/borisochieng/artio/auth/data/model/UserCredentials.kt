package dev.borisochieng.artio.auth.data.model

data class UserCredentials(
    val uid: String,
    val name: String,
    val email: String,
    val password: String
)
