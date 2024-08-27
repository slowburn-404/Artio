package dev.borisochieng.artio.ui.screens.home

import dev.borisochieng.artio.database.Sketch

data class HomeUiState(
	val localSketches: List<Sketch> = emptyList(),
	val remoteSketches: List<Sketch> = emptyList(),
	val userIsLoggedIn: Boolean = false,
	val isLoading: Boolean = true,
	val feedback: String? = null,
	val fetchedFromRemoteDb: Boolean = false
)
