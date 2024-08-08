package dev.borisochieng.sketchpad.auth.presentation.viewmodels

sealed class UiEvent {
    data class SnackBarEvent(val message: String): UiEvent()
}