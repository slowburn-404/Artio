package dev.borisochieng.sketchpad.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.auth.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signUp(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser? = authResult.user

                val user = User(
                    email = firebaseUser?.email ?: "No email"
                )
                FirebaseResponse.Success(user)

            } catch (e: Exception) {
                val error = when (e) {
                    is FirebaseAuthWeakPasswordException -> FirebaseResponse.Error("${e.reason}", e)
                    is FirebaseAuthUserCollisionException -> FirebaseResponse.Error(
                        "An account registered with the same email already exists!",
                        e
                    )

                    is FirebaseAuthException -> FirebaseResponse.Error("${e.message}", e)
                    else -> FirebaseResponse.Error("Something went wrong please try again", e)
                }
                error
            }
        }

    override suspend fun login(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
           try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser? = authResult.user

                val user = User(
                    email = firebaseUser?.email ?: "No email"
                )

                FirebaseResponse.Success(user)
            } catch (e: Exception) {
                val error = when (e) {
                    is FirebaseAuthInvalidUserException -> FirebaseResponse.Error(
                        "Invalid username or email.",
                        e
                    )

                    is FirebaseAuthInvalidCredentialsException -> FirebaseResponse.Error(
                        "Invalid credentials.",
                        e
                    )

                    is FirebaseAuthUserCollisionException -> FirebaseResponse.Error(
                        "An account with this email already exists!",
                        e
                    )

                    is FirebaseAuthException -> FirebaseResponse.Error("${e.message}", e)

                    else -> FirebaseResponse.Error("Something went wrong, please try again.", e)
                }
                error
            }
        }
}