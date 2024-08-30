package dev.borisochieng.artio.ui.screens.drawingboard.data

sealed class CanvasUiEvents {
    data class SnackBarEvent(val message: String) : CanvasUiEvents()
}
