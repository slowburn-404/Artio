package dev.borisochieng.artio.ui.screens.auth.state

sealed class UiEvent {
    data class SnackBarEvent(val message: String): UiEvent()
}