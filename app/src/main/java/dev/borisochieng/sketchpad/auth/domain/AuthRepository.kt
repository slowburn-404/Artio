package dev.borisochieng.sketchpad.auth.domain

import android.net.Uri
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.model.User

interface AuthRepository {

    suspend fun signUp(email: String, password: String): FirebaseResponse<User>

    suspend fun login(email: String, password: String): FirebaseResponse<User>

    suspend fun logout()

    suspend fun checkIfUserIsLoggedIn(): Boolean

    suspend fun updateUserProfile(displayName: String, imageUrl: Uri): FirebaseResponse<User>

    suspend fun uploadImageToFireStore(uri: Uri): FirebaseResponse<Uri>

//    suspend fun sendPasswordResetEmail(email: String): FirebaseResponse<String>

}