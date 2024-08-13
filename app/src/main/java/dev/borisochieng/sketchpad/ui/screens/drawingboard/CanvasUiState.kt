package dev.borisochieng.sketchpad.ui.screens.drawingboard

import androidx.compose.runtime.Immutable
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties

@Immutable
data class CanvasUiState(
    val boardDetails: BoardDetails? = null,
    val error: String = "",
    val paths: List<PathProperties> = emptyList()
)
