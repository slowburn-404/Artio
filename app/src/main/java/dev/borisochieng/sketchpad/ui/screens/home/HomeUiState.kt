package dev.borisochieng.sketchpad.ui.screens.home

import dev.borisochieng.sketchpad.database.Sketch

data class HomeUiState(
	val localSketches: List<Sketch> = emptyList(),
	val remoteSketches: List<Sketch> = emptyList(),
	val userIsLoggedIn: Boolean = false,
	val isLoading: Boolean = true,
	val feedback: String? = null
)
