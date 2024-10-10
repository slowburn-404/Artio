package dev.borisochieng.firebase.model

import android.net.Uri

data class User(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val imageUrl: Uri? = null,
    val isLoggedIn: Boolean
)
