package dev.borisochieng.firebase.model

data class SketchFirebase(
    val id: String = "",
    val dateCreated: String = "",
    val lastModified: String = "",
    val title: String = "",
    val paths: List<PathPropertiesFirebase> = emptyList(),
    val isBackedUp: Boolean = false
)
