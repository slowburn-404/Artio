package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

import android.net.Uri
import androidx.compose.runtime.Immutable
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.database.Sketch

@Immutable
data class CanvasUiState(
    val userIsLoggedIn: Boolean = false,
    val boardDetails: BoardDetails = BoardDetails("", "", emptyList()),
    val sketchIsBackedUp: Boolean = false,
    val error: String = "",
    val sketch: Sketch? = null,
    val collabUrl: Uri? = null,
    val paths: List<PathProperties> = emptyList(),
)
