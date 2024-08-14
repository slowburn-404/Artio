package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {

	private val sketchRepository by inject<SketchRepository>()

	private var localSketches by mutableStateOf<List<Sketch>>(emptyList()) // for internal use only
	private var synced by mutableStateOf(false)

	private val _uiState = MutableStateFlow(HomeUiState())
	var uiState by mutableStateOf(_uiState.value); private set

	init {
		viewModelScope.launch {
			_uiState.collect { uiState = it }
		}
		viewModelScope.launch {
			sketchRepository.getAllSketches().collect { sketches ->
				localSketches = sketches
				if (synced) {
					_uiState.update { it.copy(savedSketches = sketches) }
					delay(1000)
					_uiState.update { it.copy(isLoading = false) }
					return@collect
				}
				refreshDatabase()
			}
		}
	}

	fun actions(action: HomeActions) {
		when (action) {
			is HomeActions.RenameSketch -> renameSketch(action.sketch)
			is HomeActions.DeleteSketch -> deleteSketch(action.sketch)
		}
	}

	private fun refreshDatabase() {
		synced = true
		viewModelScope.launch {
//			val remoteSketches = listOf(Sketch((1..10000).random(), "Jankz", pathList = emptyList())) // for testing
			val remoteSketches = emptyList<Sketch>() // TODO(change this to the function for fetching remote sketches)
			val unsyncedSketches = localSketches.filterNot { sketch ->
				sketch.name in remoteSketches.map { it.name }
			}
			sketchRepository.refreshDatabase(remoteSketches + unsyncedSketches)
		}
	}

	private fun renameSketch(sketch: Sketch) {
		viewModelScope.launch {
			sketchRepository.updateSketch(sketch)
		}
	}

	private fun deleteSketch(sketchToDelete: Sketch) {
		viewModelScope.launch {
			sketchRepository.deleteSketch(sketchToDelete)
		}
	}

}