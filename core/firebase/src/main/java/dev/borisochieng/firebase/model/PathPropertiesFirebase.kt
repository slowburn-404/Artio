package dev.borisochieng.firebase.model

data class PathPropertiesFirebase(
    val id: String = "",
    val alpha: Float = 0f,
    val color: String = "",
    val eraseMode: Boolean = false,
    val start: OffsetFirebase = OffsetFirebase(0f, 0f),
    val end: OffsetFirebase = OffsetFirebase(0f,0f),
    val strokeWidth: Float = 2f
)