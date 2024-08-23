package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
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
    private val firebaseUser by inject<FirebaseUser>()

	private var localSketches by mutableStateOf<List<Sketch>>(emptyList()) // for internal use only
	private var synced by mutableStateOf(false)

	private val _uiState = MutableStateFlow(HomeUiState())
	var uiState by mutableStateOf(_uiState.value); private set

	init {
		isLoggedIn(warmCheck = false)
		fallbackPlan()

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
			is HomeActions.Refresh -> refreshDatabase()
			is HomeActions.ClearFeedback -> _uiState.update { it.copy(feedback = null) }
		}
	}

	private fun saveSketchToRemoteDb(sketch: Sketch) {
		viewModelScope.launch {
			if (!authRepository.checkIfUserIsLoggedIn()) return@launch

			val response = collabRepository.createSketch(
				userId = firebaseUser.uid,
				sketch = sketch.toDBSketch()
			)
			when (response) {
				is FirebaseResponse.Success -> {
					_uiState.update {
						it.copy(
							remoteSketches = fetchSketchesFromRemoteDB(),
							feedback = "'${sketch.name}' successfully backed up"
						)
					}
				}

				is FirebaseResponse.Error -> {
					_uiState.update { it.copy(feedback = response.message) }
				}
			}
		}
	}

	private fun refreshDatabase() {
		synced = true
		_uiState.update { it.copy(fetchedFromRemoteDb = false) }
		viewModelScope.launch {
//			val remoteSketches = listOf(Sketch(name = "Jankz", pathList = emptyList())) // for testing
			val remoteSketches = fetchSketchesFromRemoteDB()
			val unsyncedSketches = localSketches.filterNot { sketch ->
				sketch.name in remoteSketches.map { it.name }
			}
			sketchRepository.refreshDatabase(remoteSketches + unsyncedSketches)
			_uiState.update { it.copy(
				remoteSketches = remoteSketches,
				fetchedFromRemoteDb = true
			) }
		}
	}

    private fun renameSketch(sketch: Sketch) {
        viewModelScope.launch {
            sketchRepository.updateSketch(sketch)
            if (!uiState.userIsLoggedIn) return@launch
            renameSketchInRemoteDB(userId = firebaseUser.uid, boardId = sketch.id, title = sketch.name)
        }
    }

    private fun renameSketchInRemoteDB(
        userId: String,
        boardId: String,
        title: String,
    ) = viewModelScope.launch {
        val renameTask = collabRepository.renameSketchInRemoteDB(
            userId = userId,
            boardId = boardId,
            title = title
        )

        when (renameTask) {
            is FirebaseResponse.Success -> {
                _uiState.update {
                    it.copy(
                        feedback = renameTask.data
                    )
                }
            }

            is FirebaseResponse.Error -> {
                _uiState.update {
                    it.copy(
                        feedback = renameTask.message
                    )
                }

            }
        }
    }

    private fun deleteSketch(sketchToDelete: Sketch) {
        viewModelScope.launch {
            sketchRepository.deleteSketch(sketchToDelete)

            if (!_uiState.value.userIsLoggedIn) return@launch
            val selectedSKetchIndex =
                _uiState.value.remoteSketches.indexOfFirst { it == sketchToDelete }
            if (selectedSKetchIndex != 1) {
                deleteSketchFromRemoteDB(
                    userId = firebaseUser.uid,
                    boardId = sketchToDelete.id
                )
            }
        }
    }

    private fun deleteSketchFromRemoteDB(userId: String, boardId: String) =
        viewModelScope.launch {
            val deleteTask = collabRepository.deleteSketch(userId = userId, boardId = boardId)

            when (deleteTask) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            feedback = deleteTask.data
                        )
                    }
                }

                is FirebaseResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            feedback = deleteTask.message
                        )
                    }
                }
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
		val response = collabRepository.fetchExistingSketches(firebaseUser.uid)

		return when (response) {
			is FirebaseResponse.Success -> {
				response.data ?: emptyList()
			}
			else -> emptyList()
		}
	}

	// if the user is logged in but, at the start of the application,
	// there is poor or no internet connection, the attempt to fetch all sketches from remote db
	// continues indefinitely, till there is stable internet connection. This causes Loading UI
	// to be displayed indefinitely as well. This is a fallback plan for when there is no response
	// from remote source after 10 seconds.
	private fun fallbackPlan() {
		viewModelScope.launch {
			delay(10000)
			if (!uiState.isLoading) return@launch
			_uiState.update {
				it.copy(
					localSketches = localSketches.ifEmpty { emptyList() },
					isLoading = false,
					fetchedFromRemoteDb = localSketches.isEmpty()
				)
			}
		}
	}

}