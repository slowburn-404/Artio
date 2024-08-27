package dev.borisochieng.artio.ui.screens.drawingboard.data

import android.net.Uri
import androidx.compose.runtime.Immutable
import dev.borisochieng.artio.collab.data.models.BoardDetails
import dev.borisochieng.artio.database.Sketch

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
