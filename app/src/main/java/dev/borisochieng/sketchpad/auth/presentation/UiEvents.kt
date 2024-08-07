package dev.borisochieng.sketchpad.auth.presentation

sealed class UiEvent {
    data class SnackBarEvent(val message: String): UiEvent()
}