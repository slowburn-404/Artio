package dev.borisochieng.artio.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.firebase.auth.data.FirebaseResponse
import dev.borisochieng.firebase.auth.domain.AuthRepository
import dev.borisochieng.firebase.auth.domain.model.User
import dev.borisochieng.firebase.database.data.toDBSketch
import dev.borisochieng.firebase.database.domain.CollabRepository
import dev.borisochieng.database.database.Sketch
import dev.borisochieng.database.database.repository.SketchRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {

    private val authRepository by inject<dev.borisochieng.firebase.auth.domain.AuthRepository>()
    private val sketchRepository by inject<dev.borisochieng.database.database.repository.SketchRepository>()
    private val collabRepository by inject<dev.borisochieng.firebase.database.domain.CollabRepository>()
    private val firebaseUser by inject<FirebaseUser>()

    private var localSketches by mutableStateOf<List<dev.borisochieng.database.database.Sketch>>(emptyList()) // for internal use only
    private var synced by mutableStateOf(false)

    private val _uiState = MutableStateFlow(HomeUiState())
    var uiState by mutableStateOf(_uiState.value); private set

    init {
        isLoggedIn(warmCheck = false)
        fallbackPlan()
        getUserDetails()

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
            is HomeActions.Refresh -> {
                refreshDatabase()
                getUserDetails()
            }
            is HomeActions.ClearFeedback -> _uiState.update { it.copy(feedback = null) }
        }
    }

    private fun saveSketchToRemoteDb(sketch: dev.borisochieng.database.database.Sketch) {
        viewModelScope.launch {
            if (!authRepository.checkIfUserIsLoggedIn()) return@launch

            val response = collabRepository.createSketch(
                userId = firebaseUser.uid,
                sketch = sketch.toDBSketch()
            )
            when (response) {
                is dev.borisochieng.firebase.auth.data.FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            remoteSketches = fetchSketchesFromRemoteDB(),
                            feedback = "'${sketch.name}' successfully backed up"
                        )
                    }
                }

                is dev.borisochieng.firebase.auth.data.FirebaseResponse.Error -> {
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
            _uiState.update {
                it.copy(
                    remoteSketches = remoteSketches,
                    fetchedFromRemoteDb = true
                )
            }
        }
    }

    private fun renameSketch(sketch: dev.borisochieng.database.database.Sketch) {
        viewModelScope.launch {
            sketchRepository.updateSketch(sketch)
            if (!uiState.userIsLoggedIn) return@launch
            renameSketchInRemoteDB(
                userId = firebaseUser.uid,
                boardId = sketch.id,
                title = sketch.name
            )
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
            is dev.borisochieng.firebase.auth.data.FirebaseResponse.Success -> {
                _uiState.update {
                    it.copy(
                        feedback = renameTask.data
                    )
                }
            }

            is dev.borisochieng.firebase.auth.data.FirebaseResponse.Error -> {
                _uiState.update {
                    it.copy(
                        feedback = renameTask.message
                    )
                }

            }
        }
    }

    private fun deleteSketch(sketchToDelete: dev.borisochieng.database.database.Sketch) {
        viewModelScope.launch {
            sketchRepository.deleteSketch(sketchToDelete)

            if (!_uiState.value.userIsLoggedIn) return@launch
            val selectedSKetchIndex =
                _uiState.value.remoteSketches.indexOfFirst { it == sketchToDelete }
            if (selectedSKetchIndex != -1) {
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
                is dev.borisochieng.firebase.auth.data.FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            feedback = deleteTask.data
                        )
                    }
                }

                is dev.borisochieng.firebase.auth.data.FirebaseResponse.Error -> {
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
        if (!warmCheck) return@launch
        if (response) refreshDatabase() else {
            _uiState.update { it.copy(remoteSketches = emptyList()) }
        }
        getUserDetails()
    }

    private suspend fun fetchSketchesFromRemoteDB(): List<dev.borisochieng.database.database.Sketch> {
        if (!authRepository.checkIfUserIsLoggedIn()) return emptyList()
        val response = collabRepository.fetchExistingSketches(firebaseUser.uid)

        return when (response) {
            is dev.borisochieng.firebase.auth.data.FirebaseResponse.Success -> {
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

    private fun getUserDetails() =
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                _uiState.update { state ->
                    state.copy(
                        user = dev.borisochieng.firebase.auth.domain.model.User(
                            uid = user.uid,
                            email = user.email!!,
                            displayName = user.displayName,
                            imageUrl = user.photoUrl,
                            isLoggedIn = true
                        )
                    )
                }
            } ?: _uiState.update { it.copy(user = null) }
        }

}