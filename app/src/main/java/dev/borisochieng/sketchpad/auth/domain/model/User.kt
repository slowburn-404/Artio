package dev.borisochieng.sketchpad.auth.domain.model

import android.net.Uri

data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val imageUrl: Uri
)
