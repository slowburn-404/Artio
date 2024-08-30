package dev.borisochieng.artio.collab.data.models

data class DBPathProperties(
    val id: String = "",
    val alpha: Float = 0f,
    val color: String = "",
    val eraseMode: Boolean = false,
    val start: DBOffset = DBOffset(0f, 0f) ,
    val end: DBOffset = DBOffset(0f,0f),
    val strokeWidth: Float = 2f
)