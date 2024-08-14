package dev.borisochieng.sketchpad.ui.screens.home

import dev.borisochieng.sketchpad.database.Sketch

data class HomeUiState(
	val savedSketches: List<Sketch> = emptyList(),
	val isLoading: Boolean = true
)
