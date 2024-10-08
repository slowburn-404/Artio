package dev.borisochieng.artio.ui.screens.auth.state

import androidx.compose.runtime.Immutable
import dev.borisochieng.firebase.auth.domain.model.User

@Immutable
data class UiState(
    val user: dev.borisochieng.firebase.auth.domain.model.User? = dev.borisochieng.firebase.auth.domain.model.User(
        "",
        "",
        "",
        null,
        false
    ),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String = ""
)
