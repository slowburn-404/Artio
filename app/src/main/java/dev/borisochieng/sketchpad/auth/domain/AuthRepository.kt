package dev.borisochieng.sketchpad.auth.domain

import android.net.Uri
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.model.User

interface AuthRepository {

    suspend fun signUp(email: String, password: String): FirebaseResponse<User>

    suspend fun login(email: String, password: String): FirebaseResponse<User>

    suspend fun logout()

    suspend fun checkIfUserIsLoggedIn(): FirebaseResponse<User>

    suspend fun updateUserProfile(displayName: String, imageUrl: String,): FirebaseResponse<User>

    suspend fun uploadImageToFireStore(uri: Uri, onUploadSuccess: (String) -> Unit, onUploadFailure: (Exception) -> Unit)

}