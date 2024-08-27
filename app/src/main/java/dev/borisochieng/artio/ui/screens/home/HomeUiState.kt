package dev.borisochieng.artio.ui.screens.home

import dev.borisochieng.artio.auth.domain.model.User
import dev.borisochieng.artio.database.Sketch

data class HomeUiState(
	val localSketches: List<Sketch> = emptyList(),
	val remoteSketches: List<Sketch> = emptyList(),
	val userIsLoggedIn: Boolean = false,
	val isLoading: Boolean = true,
	val feedback: String? = null,
	val fetchedFromRemoteDb: Boolean = false,
	val user: User? = null
)
