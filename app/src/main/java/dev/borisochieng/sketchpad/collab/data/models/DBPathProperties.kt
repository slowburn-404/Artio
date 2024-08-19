package dev.borisochieng.sketchpad.collab.data.models

import java.util.UUID.randomUUID

data class DBPathProperties(
    val id: String = randomUUID().toString(),
    val alpha: Float = 0f,
    val color: String = "",
    val eraseMode: Boolean = false,
    val start: DBOffset = DBOffset(0f, 0f) ,
    val end: DBOffset = DBOffset(0f,0f),
    val strokeWidth: Float = 2f
)