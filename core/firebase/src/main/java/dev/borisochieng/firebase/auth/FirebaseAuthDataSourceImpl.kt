package dev.borisochieng.firebase.auth

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import dev.borisochieng.firebase.FirebaseResponse
import dev.borisochieng.firebase.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID.randomUUID

internal class FirebaseAuthDataSourceImpl: FirebaseAuthDataSource, KoinComponent {

    private val firebaseAuth by inject<FirebaseAuth>()

    //private lateinit var actionCodeSettings: ActionCodeSettings

    override suspend fun signUp(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser? = authResult.user

                val newUser =
                    User(
                        uid = firebaseUser?.uid!!,
                        displayName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email!!,
                        imageUrl = firebaseUser.photoUrl,
                        isLoggedIn = checkIfUserIsLoggedIn()
                    )

                Log.d("New User", newUser.toString())
                FirebaseResponse.Success(newUser)

            } catch (e: Exception) {
                e.printStackTrace()
                val error = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Choose a stronger password"
                    is FirebaseAuthUserCollisionException -> "An account registered with the same email already exists!"

                    is FirebaseAuthException -> "Something went wrong please try again later"
                    else -> "Something went wrong please try again later"
                }
                FirebaseResponse.Error(error)
            }
        }

    override suspend fun login(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser? = authResult.user
                val user =
                    User(
                        uid = firebaseUser?.uid!!,
                        email = firebaseUser.email!!,
                        displayName = firebaseUser.displayName ?: "",
                        imageUrl = firebaseUser.photoUrl,
                        isLoggedIn = checkIfUserIsLoggedIn()

                    )
                Log.d("User", user.toString())
                FirebaseResponse.Success(user)
            } catch (e: Exception) {
                e.printStackTrace()
                val error = when (e) {
                    is FirebaseAuthInvalidUserException -> "The user does not exist"

                    is FirebaseAuthInvalidCredentialsException -> "Invalid credentials"

                    is FirebaseAuthUserCollisionException -> "An account with these credentials already exists!"

                    is FirebaseAuthException -> "Something went wrong please try again later"

                    else -> "Something went wrong, please try again."
                }
                FirebaseResponse.Error(error)
            }
        }

    override suspend fun logout() =
        withContext(Dispatchers.IO) {
            firebaseAuth.signOut()
        }

    override suspend fun checkIfUserIsLoggedIn(): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext firebaseAuth.currentUser != null
        }

    override suspend fun updateUserProfile(
        displayName: String,
        imageUrl: Uri
    ): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser

                if (currentUser != null) {
                    val profileUpdates =
                        UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(displayName)
                            .setPhotoUri(imageUrl)
                            .build()

                    currentUser.updateProfile(profileUpdates).await()

                    val updatedUser = User(
                        uid = currentUser.uid,
                        displayName = currentUser.displayName,
                        imageUrl = currentUser.photoUrl,
                        email = currentUser.email!!, //cannot be null since account was created with an email
                        isLoggedIn = checkIfUserIsLoggedIn()
                    )
                    FirebaseResponse.Success(updatedUser)
                } else {
                    FirebaseResponse.Error("No user is logged in")
                }
            } catch (e: Exception) {
                e.printStackTrace()

                FirebaseResponse.Error("Failed to update profile, please try again")

            }
        }

    override suspend fun uploadImageToFireStore(
        uri: Uri,
    ): FirebaseResponse<Uri> =
        withContext(Dispatchers.IO) {
            try {
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("images/${randomUUID()}")
                storageReference.putFile(uri).await()

                val downloadUrl = storageReference.downloadUrl.await()

                FirebaseResponse.Success(downloadUrl)

            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseResponse.Error("Image upload failed please try again")

            }
        }

    override suspend fun sendPasswordResetEmail(email: String): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            try {
//                actionCodeSettings = actionCodeSettings {
//                    url = "https://auth.sketchpad.jc/?code"
//                    handleCodeInApp = true
//                    setAndroidPackageName(
//                        "dev.borisochieng.sketchpad",
//                        true, //install if not available,
//                        "7"// minimum version
//                    )
//                }
                firebaseAuth.sendPasswordResetEmail(
                    email
                ).await()

                FirebaseResponse.Success("Email sent!")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Send email", e.message.toString())

                FirebaseResponse.Error("Failed to send email, please try again")

            }
        }
}