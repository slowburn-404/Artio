package dev.borisochieng.sketchpad.collab.data.models

data class DBSketch(
    val id: String = "",
    val dateCreated: String = "",
    val lastModified: String = "",
    val title: String = "",
    val paths: List<DBPathProperties> = emptyList()

)
