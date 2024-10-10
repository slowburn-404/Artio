package dev.borisochieng.firebase.auth

import android.net.Uri
import dev.borisochieng.firebase.FirebaseResponse
import dev.borisochieng.firebase.model.User

interface FirebaseAuthDataSource {

    suspend fun signUp(email: String, password: String): FirebaseResponse<User>

    suspend fun login(email: String, password: String): FirebaseResponse<User>

    suspend fun logout()

    suspend fun checkIfUserIsLoggedIn(): Boolean

    suspend fun updateUserProfile(displayName: String, imageUrl: Uri): FirebaseResponse<User>

    suspend fun uploadImageToFireStore(uri: Uri): FirebaseResponse<Uri>

    suspend fun sendPasswordResetEmail(email: String): FirebaseResponse<String>

}