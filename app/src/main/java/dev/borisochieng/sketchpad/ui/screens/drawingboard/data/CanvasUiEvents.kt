package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

sealed class CanvasUiEvents {
    data class SnackBarEvent(val message: String) : CanvasUiEvents()
}
