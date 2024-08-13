package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.collab.data.toDBSketch
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

    private val sketchRepository by inject<SketchRepository>()

    private val collabRepository by inject<CollabRepository>()

    private val firebaseUser by inject<FirebaseUser>()

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> get() = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<CanvasUiEvents>()
    val uiEvents: SharedFlow<CanvasUiEvents> get() = _uiEvents.asSharedFlow()

    var sketch by mutableStateOf<Sketch?>(null); private set

//<<<<<<< HEAD
//	private fun saveSketch(sketch: Sketch) {
//		viewModelScope.launch {
//			sketchRepository.saveSketch(sketch)
//			saveSketchToRemoteDb(sketch)
//		}
//	}

	private fun saveSketchToRemoteDb(sketch: Sketch) {
		viewModelScope.launch {
			try {
				val dbSketch = sketch.toDBSketch()
				collabRepository.createSketch(
					userId = firebaseUser.uid,
					title = dbSketch.title,
					paths = dbSketch.paths
				)
			} catch (e: Exception) {
				Log.e("RemoteDbError", "User is not logged in", e)
			}
		}
	}

    fun fetchSketch(sketchId: Int) {
        sketch = null
        viewModelScope.launch {
            sketchRepository.getSketch(sketchId).collect {
                sketch = it
            }
        }
    }

    fun actions(action: SketchPadActions) {
        when (action) {
            is SketchPadActions.SaveSketch -> saveSketch(action.sketch)
            is SketchPadActions.UpdateSketch -> updateSketch(action.paths)
            SketchPadActions.SketchClosed -> sketch = null
            SketchPadActions.SketchClosed -> sketch = null
        }
    }

    private fun saveSketch(sketch: Sketch) {
        viewModelScope.launch {
            sketchRepository.saveSketch(sketch)
            val dbSketch = sketch.toDBSketch()
            val response = collabRepository.createSketch(
                userId = firebaseUser.uid,
                title = dbSketch.title,
                paths = dbSketch.paths
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

                    _uiState.update {
                        it.copy(
                            error = response.message
                        )
                    }

                    _uiEvents.emit(CanvasUiEvents.SnackBarEvent(response.message))

                }
            }
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

    fun listenForSketchChanges() =
        viewModelScope.launch {
            val boardId = _uiState.value.boardDetails?.boardId
            if (boardId != null) {
                val response = collabRepository.listenForSketchChanges(
                    userId = firebaseUser.uid,
                    boardId = boardId
                )

                response.collectLatest { dbResponse ->
                    when(dbResponse) {
                        is FirebaseResponse.Success -> {
                            _uiState.update {
                                it.copy(
                                    error = "",
                                    paths = dbResponse.data ?: emptyList()
                                )
                            }

                        }
                        is FirebaseResponse.Error -> {

                            _uiState.update {
                                it.copy(
                                    error = dbResponse.message
                                )
                            }

                            _uiEvents.emit(CanvasUiEvents.SnackBarEvent(dbResponse.message))

                        }
                    }
                }
            }

        }

    fun updatePathInDb(path: PathProperties ) =
        viewModelScope.launch {
            val boardDetails = _uiState.value.boardDetails
            if(boardDetails != null) {
                val response = collabRepository.updatePathInDB(
                    userId = boardDetails.userId,
                    boardId = boardDetails.boardId,
                    path = path,
                    pathId = boardDetails.pathIds.first()
                )

                if(response is FirebaseResponse.Error) {
                    _uiState.update {
                        it.copy(
                            error = response.message
                        )
                    }
                    _uiEvents.emit(CanvasUiEvents.SnackBarEvent(response.message))
                }
            }
        }
}