package dev.borisochieng.sketchpad.auth.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.auth.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID.randomUUID

class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signUp(email: String, password: String): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser? = authResult.user

                val newUser = User(
                    uid = firebaseUser?.uid ?: "",
                    displayName = firebaseUser?.displayName ?: "",
                    email = firebaseUser?.email ?: "",
                    imageUrl = firebaseUser?.photoUrl.toString()
                )
                FirebaseResponse.Success(newUser)

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
                var user: User? = null

                firebaseUser?.let {
                    user = User(
                        uid = it.uid,
                        email = it.email!!,
                        displayName = it.displayName!!,
                        imageUrl = it.photoUrl.toString()
                    )
                }

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

    override suspend fun logout() =
        withContext(Dispatchers.IO) {
            firebaseAuth.signOut()
        }

    override suspend fun checkIfUserIsLoggedIn(): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                var user: User? = null

                currentUser?.let {
                    user = User(
                        uid = it.uid,
                        email = it.email!!,
                        displayName = it.displayName ?: "",
                        imageUrl = it.photoUrl.toString()
                    )
                }

                FirebaseResponse.Success(user)


            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseResponse.Error(e.message.toString(), e)
            }
        }

    override suspend fun updateUserProfile(
        displayName: String,
        imageUrl: String
    ): FirebaseResponse<User> =
        withContext(Dispatchers.IO) {
            try {
                val firebaseUser = firebaseAuth.currentUser

                if(firebaseUser != null) {
                    val profileUpdates =
                        UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(displayName)
                            .setPhotoUri(imageUrl.let { Uri.parse(it) }).build()

                    firebaseUser.updateProfile(profileUpdates).await()

                    val updatedUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        imageUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    FirebaseResponse.Success(updatedUser)
                } else {
                    FirebaseResponse.Error("No user is logged in")
                }
            } catch (e: Exception) {
                e.printStackTrace()

                FirebaseResponse.Error("Failed to update profile, please try again", e)

            }
        }

    override suspend fun uploadImageToFireStore(
        uri: Uri,
        onUploadSuccess: (String) -> Unit,
        onUploadFailure: (Exception) -> Unit
    ) {
     val storageReference = FirebaseStorage.getInstance().reference.child("images/${randomUUID()}")
        val uploadTask = storageReference.putFile(uri)

        uploadTask.continueWithTask { task ->
            if(!task.isSuccessful) {
                task.exception?.let{ throw it}
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val downloadUri = task.result
                onUploadSuccess(downloadUri.toString())
            } else {
                task.exception?.let {onUploadFailure(it)}
            }
        }
    }


}