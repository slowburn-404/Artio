package dev.borisochieng.artio.collab.data.models

data class BoardDetails(
    val userId: String = "",
    val boardId: String = "",
    val pathIds: List<String> = emptyList()
)
