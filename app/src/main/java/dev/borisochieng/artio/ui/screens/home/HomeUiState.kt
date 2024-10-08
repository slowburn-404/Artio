package dev.borisochieng.artio.ui.screens.home

import dev.borisochieng.firebase.auth.domain.model.User
import dev.borisochieng.database.database.Sketch

data class HomeUiState(
    val localSketches: List<dev.borisochieng.database.database.Sketch> = emptyList(),
    val remoteSketches: List<dev.borisochieng.database.database.Sketch> = emptyList(),
    val userIsLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val feedback: String? = null,
    val fetchedFromRemoteDb: Boolean = false,
    val user: dev.borisochieng.firebase.auth.domain.model.User? = null
)
