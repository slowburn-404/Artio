package dev.borisochieng.sketchpad.ui.screens.drawingboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.collab.data.toDBPathProperties
import dev.borisochieng.sketchpad.collab.data.toDBSketch
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SketchPadViewModel : ViewModel(), KoinComponent {

    private val authRepository: AuthRepository by inject()

    private val sketchRepository by inject<SketchRepository>()

    private val collabRepository by inject<CollabRepository>()

    private val firebaseUser by inject<FirebaseUser>()

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> get() = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<CanvasUiEvents>()
    val uiEvents: SharedFlow<CanvasUiEvents> get() = _uiEvents.asSharedFlow()

    var sketch by mutableStateOf<Sketch?>(null); private set

    init {
        isLoggedIn()
        listenForSketchChanges()
    }

	fun fetchSketch(sketchId: String) {
		sketch = null
		viewModelScope.launch {
			sketchRepository.getSketch(sketchId).collect { fetchedSketch ->
				sketch = fetchedSketch
				val remoteSketches = fetchSketchesFromRemoteDB()
				_uiState.update { it.copy(sketchIsBackedUp = sketch in remoteSketches) }
			}
		}
	}

	fun actions(action: SketchPadActions) {
		when (action) {
			is SketchPadActions.SaveSketch -> saveSketch(action.sketch)
			is SketchPadActions.UpdateSketch -> updateSketch(action.paths)
			SketchPadActions.CheckIfUserIsLoggedIn -> isLoggedIn()
			SketchPadActions.SketchClosed -> sketch = null
		}
	}

    private fun saveSketch(sketch: Sketch) {
        viewModelScope.launch {
            sketchRepository.saveSketch(sketch)
            if (!authRepository.checkIfUserIsLoggedIn()) return@launch
            saveSketchToRemoteDb(sketch)
        }
    }

	private fun updateSketch(paths: List<PathProperties>) {
		viewModelScope.launch {
			if (sketch == null) return@launch
			val updatedSketch = Sketch(
				id = sketch!!.id,
				name = sketch!!.name,
				dateCreated = sketch!!.dateCreated,
				pathList = paths
			)
			sketchRepository.updateSketch(updatedSketch)
		}
	}
    
    private fun saveSketchToRemoteDb(sketch: Sketch) {
		viewModelScope.launch {
			val dbSketch = sketch.toDBSketch()
			val response = collabRepository.createSketch(
				userId = firebaseUser.uid,
				sketch = dbSketch
			)

            when (response) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            boardDetails = response.data,
                            error = ""
                        )
                    }
                }

                is FirebaseResponse.Error -> {
                    _uiState.update { it.copy(error = response.message) }
                    _uiEvents.emit(CanvasUiEvents.SnackBarEvent(response.message))
                }
            }
        }
    }

    private fun isLoggedIn() = viewModelScope.launch {
        val response = authRepository.checkIfUserIsLoggedIn()
        _uiState.update { it.copy(userIsLoggedIn = response) }
    }

    private fun listenForSketchChanges() =
        viewModelScope.launch {
            val boardId = _uiState.value.boardDetails?.boardId
            if (boardId != null) {
                val response = collabRepository.listenForSketchChanges(
                    userId = firebaseUser.uid,
                    boardId = boardId
                )
                response.collectLatest { dbResponse ->
                    when (dbResponse) {
                        is FirebaseResponse.Success -> {
                            _uiState.update {
                                it.copy(
	                                sketchIsBackedUp = true,
                                    error = "",
                                    paths = dbResponse.data ?: emptyList()
                                )
                            }
                        }

                        is FirebaseResponse.Error -> {
                            _uiState.update { it.copy(error = dbResponse.message) }
                            _uiEvents.emit(CanvasUiEvents.SnackBarEvent(dbResponse.message))

                        }
                    }
                }
            }
        }

    fun updatePathInDb(paths: List<PathProperties>) =
        viewModelScope.launch {
            val boardDetails = _uiState.value.boardDetails
            if (boardDetails != null && paths.isNotEmpty()) {
                val pathIds =
                    boardDetails.pathIds.take(paths.size) //ensure paths id matches path count
                val response =
                    collabRepository.updatePathInDB(
                        userId = boardDetails.userId,
                        boardId = boardDetails.boardId,
                        paths = paths.map { it.toDBPathProperties() },
                        pathIds = pathIds
                    )

                if (response is FirebaseResponse.Error) {
                    _uiState.update { it.copy(error = response.message) }
                    _uiEvents.emit(CanvasUiEvents.SnackBarEvent(response.message))
                }
            }
        }

    fun generateCollabUrl(userId: String, boardId: String) =
        viewModelScope.launch {
            val uri = collabRepository.generateCollabUrl(userId, boardId)
            _uiState.update { it.copy(collabUrl = uri) }
        }

	private suspend fun fetchSketchesFromRemoteDB(): List<Sketch> {
		if (!authRepository.checkIfUserIsLoggedIn()) return emptyList()
		val response = firebaseUser.let { collabRepository.fetchExistingSketches(it.uid) }

		return when (response) {
			is FirebaseResponse.Success -> response.data ?: emptyList()
			else -> emptyList()
		}
	}

}