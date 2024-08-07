package dev.borisochieng.sketchpad.auth.domain

import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.data.model.UserCredentials
import dev.borisochieng.sketchpad.auth.domain.model.User

interface SignUpRepository {

    suspend fun signUp(email: String, password: String): FirebaseResponse<User>
}