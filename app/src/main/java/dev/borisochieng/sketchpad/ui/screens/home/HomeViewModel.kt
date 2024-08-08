package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {

	private val sketchRepository by inject<SketchRepository>()

	var savedSketches by mutableStateOf<List<Sketch>>(emptyList()); private set

	init {
		viewModelScope.launch {
			sketchRepository.getAllSketches().collect {
				savedSketches = it
			}
		}
	}

}