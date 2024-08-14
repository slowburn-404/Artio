package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.net.Uri
import androidx.compose.runtime.Immutable
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties

@Immutable
data class CanvasUiState(
    val sketch: Sketch? = null,
    val userIsLoggedIn: Boolean = false,
    val boardDetails: BoardDetails? = null,
    val error: String = "",
    val paths: List<PathProperties> = emptyList(),
    val collabUrl: Uri? = null
)
