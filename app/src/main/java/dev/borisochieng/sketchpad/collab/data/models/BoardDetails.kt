package dev.borisochieng.sketchpad.collab.data.models

data class BoardDetails(
    val userId: String,
    val boardId: String,
    val pathIds: List<String>
)
