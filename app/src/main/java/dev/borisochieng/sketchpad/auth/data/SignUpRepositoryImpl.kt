package dev.borisochieng.sketchpad.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dev.borisochieng.sketchpad.auth.data.model.UserCredentials
import dev.borisochieng.sketchpad.auth.domain.SignUpRepository
import dev.borisochieng.sketchpad.auth.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpRepositoryImpl: SignUpRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signUp(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user  = User(authResult.user?.email ?: "No email")
                FirebaseResponse.Success(user)

            } catch (e: Exception) {
                val error = when(e) {
                    is FirebaseAuthWeakPasswordException -> FirebaseResponse.Error("${e.reason}", e)
                    is FirebaseAuthUserCollisionException-> FirebaseResponse.Error("An account with the  email: ${e.email} already exists!", e)
                    is FirebaseAuthException -> FirebaseResponse.Error("${e.message}", e)
                    else -> FirebaseResponse.Error("Something went wrong please try again", e)
                }
                error
            }
        }

}