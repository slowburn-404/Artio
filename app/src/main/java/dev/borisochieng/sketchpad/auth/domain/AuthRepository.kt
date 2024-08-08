package dev.borisochieng.sketchpad.auth.domain

import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.model.User

interface AuthRepository {

    suspend fun signUp(email: String, password: String): FirebaseResponse<User>

    suspend fun login(email: String, password: String): FirebaseResponse<User>
}