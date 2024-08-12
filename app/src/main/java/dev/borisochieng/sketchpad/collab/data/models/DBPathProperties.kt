package dev.borisochieng.sketchpad.collab.data.models

data class DBPathProperties(
    val alpha: Double,
    val color: String,
    val eraseMode: Boolean,
    val start: DBOffset,
    val end: DBOffset,
    val strokeWidth: Double
)