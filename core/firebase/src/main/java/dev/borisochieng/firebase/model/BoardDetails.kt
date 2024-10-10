package dev.borisochieng.firebase.model

data class BoardDetails(
    val userId: String = "",
    val boardId: String = "",
    val pathIds: List<String> = emptyList()
)
