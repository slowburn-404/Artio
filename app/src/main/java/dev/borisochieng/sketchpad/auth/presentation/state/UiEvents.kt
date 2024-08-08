package dev.borisochieng.sketchpad.auth.presentation.state

sealed class UiEvent {
    data class SnackBarEvent(val message: String): UiEvent()
}