package dev.borisochieng.firebase.auth.domain.model

import android.net.Uri

data class User(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val imageUrl: Uri? = null,
    val isLoggedIn: Boolean
)
