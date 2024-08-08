package dev.borisochieng.sketchpad.auth.presentation.state

import androidx.compose.runtime.Immutable
import dev.borisochieng.sketchpad.auth.domain.model.User

@Immutable
data class UiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)
