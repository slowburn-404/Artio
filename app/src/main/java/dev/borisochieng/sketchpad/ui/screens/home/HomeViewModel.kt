package dev.borisochieng.sketchpad.ui.screens.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.collab.data.toDBSketch
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {

	private val authRepository by inject<AuthRepository>()
    private val sketchRepository by inject<SketchRepository>()
    private val collabRepository by inject<CollabRepository>()
    private val firebaseUser = FirebaseAuth.getInstance().currentUser

	private var localSketches by mutableStateOf<List<Sketch>>(emptyList()) // for internal use only
	private var synced by mutableStateOf(false)

	private val _uiState = MutableStateFlow(HomeUiState())
	var uiState by mutableStateOf(_uiState.value); private set

	init {
		isLoggedIn(warmCheck = false)

		viewModelScope.launch {
			_uiState.collect { uiState = it }
		}
		viewModelScope.launch {
			sketchRepository.getAllSketches().collect { sketches ->
				localSketches = sketches
				if (synced) {
					_uiState.update { it.copy(localSketches = sketches) }
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
			is HomeActions.BackupSketch -> saveSketchToRemoteDb(action.sketch)
			is HomeActions.RenameSketch -> renameSketch(action.sketch)
			is HomeActions.DeleteSketch -> deleteSketch(action.sketch)
			is HomeActions.CheckIfUserIsLogged -> isLoggedIn(warmCheck = true)
			is HomeActions.ClearFeedback -> _uiState.update { it.copy(feedback = null) }
		}
	}

	private fun saveSketchToRemoteDb(sketch: Sketch) {
		viewModelScope.launch {
			if (!authRepository.checkIfUserIsLoggedIn()) return@launch

			firebaseUser?.let {
				val response = collabRepository.createSketch(
					userId = it.uid,
					sketch = sketch.toDBSketch()
				)

				when (response) {
					is FirebaseResponse.Success -> {
						_uiState.update {
							it.copy(feedback = "'${sketch.name}' successfully backed up")
						}
					}

					is FirebaseResponse.Error -> {
						_uiState.update { it.copy(feedback = response.message) }
					}
				}
			}
		}
	}

	private fun refreshDatabase() {
		synced = true
		viewModelScope.launch {
//			val remoteSketches = listOf(Sketch(name = "Jankz", pathList = emptyList())) // for testing
			val remoteSketches = fetchSketchesFromRemoteDB()
			val unsyncedSketches = localSketches.filterNot { sketch ->
				sketch.name in remoteSketches.map { it.name }
			}
			sketchRepository.refreshDatabase(remoteSketches + unsyncedSketches)
			_uiState.update { it.copy(remoteSketches = remoteSketches) }
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

	private fun isLoggedIn(warmCheck: Boolean) = viewModelScope.launch {
		val response = authRepository.checkIfUserIsLoggedIn()
		if (uiState.userIsLoggedIn == response) return@launch
		_uiState.update { it.copy(userIsLoggedIn = response) }
		// if userIsLoggedIn and function isn't triggered on cold start...
		if (response && warmCheck) refreshDatabase()
	}

	private suspend fun fetchSketchesFromRemoteDB(): List<Sketch> {
		if (!authRepository.checkIfUserIsLoggedIn()) return emptyList()
		val response = firebaseUser?.let { collabRepository.fetchExistingSketches(it.uid) }

		return when (response) {
			is FirebaseResponse.Success -> {
				Log.i("SketchInfo", "Remote home sketches: ${response.data}")
				response.data ?: emptyList()
			}
			is FirebaseResponse.Error -> {
				Log.i("SketchInfo", "Remote home sketches: ${response.message}")
				emptyList()
			}
			else -> emptyList()
		}
	}

}