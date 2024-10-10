package dev.borisochieng.artio.ui.screens.drawingboard.data

import android.net.Uri
import androidx.compose.runtime.Immutable
import dev.borisochieng.firebase.database.data.models.BoardDetails
import dev.borisochieng.database.database.Sketch

@Immutable
data class CanvasUiState(
    val userIsLoggedIn: Boolean = false,
    val boardDetails: dev.borisochieng.firebase.database.data.models.BoardDetails = dev.borisochieng.firebase.database.data.models.BoardDetails(
        "",
        "",
        emptyList()
    ),
    val sketchIsBackedUp: Boolean = false,
    val error: String = "",
    val sketch: dev.borisochieng.database.database.Sketch? = null,
    val collabUrl: Uri? = null,
    val paths: List<dev.borisochieng.model.PathProperties> = emptyList(),
)
