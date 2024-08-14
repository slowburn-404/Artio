package dev.borisochieng.sketchpad.collab.data.models

data class DBPathProperties(
    val alpha: Double = 0.0,
    val color: String = "",
    val eraseMode: Boolean = false,
    val start: DBOffset = DBOffset(0.0, 0.0) ,
    val end: DBOffset = DBOffset(0.0,0.0),
    val strokeWidth: Double = 0.0
)