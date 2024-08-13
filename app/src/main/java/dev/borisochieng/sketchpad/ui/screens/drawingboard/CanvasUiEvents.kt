package dev.borisochieng.sketchpad.ui.screens.drawingboard

sealed class CanvasUiEvents {
    data class SnackBarEvent(val message: String) : CanvasUiEvents()
}
