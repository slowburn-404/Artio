package dev.borisochieng.sketchpad.ui.screens.auth.state

import androidx.compose.runtime.Immutable
import dev.borisochieng.sketchpad.auth.domain.model.User

@Immutable
data class UiState(
    val user: User? = User("","","", null, false),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String = ""
)
