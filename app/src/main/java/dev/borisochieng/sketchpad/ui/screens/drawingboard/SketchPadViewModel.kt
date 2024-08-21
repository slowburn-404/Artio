package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.collab.data.toDBPathProperties
import dev.borisochieng.sketchpad.collab.data.toDBSketch
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.database.MessageModel
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import dev.borisochieng.sketchpad.database.repository.TAG
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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
    var uiState by mutableStateOf(_uiState.value)

    private val _uiEvents = MutableSharedFlow<CanvasUiEvents>()
    val uiEvents: SharedFlow<CanvasUiEvents> = _uiEvents



    private val _messages = MutableStateFlow<List<MessageModel?>>(emptyList())
    val messages: StateFlow<List<MessageModel?>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    var messageState = mutableStateOf(MessageUiState())
        private set
    private val message
        get() = messageState.value.message

    //    var sketch by mutableStateOf<Sketch?>(null); private set
    private var remoteSketches by mutableStateOf<List<Sketch>>(emptyList())

    init {
        isLoggedIn()

        viewModelScope.launch {
            fetchSketchesFromRemoteDB()
        }
        viewModelScope.launch {
            _uiState.collect { uiState = it }
        }
        _typingUsers.value = emptyList()
    }

    fun fetchSketch(sketchId: String) {
        _uiState.update { it.copy(sketch = null) }
        viewModelScope.launch {
            sketchRepository.getSketch(sketchId).collect { fetchedSketch ->
                _uiState.update { it.copy(sketch = fetchedSketch) }
                //if (uiState.userIsLoggedIn) fetchSingleSketch(sketchId)
                try {
                    _uiState.update { state ->
                        state.copy(
                            sketchIsBackedUp = fetchedSketch.id in remoteSketches.map { it.id }
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(sketchIsBackedUp = false) }
                }
            }
        }
    }

    fun actions(action: SketchPadActions) {
        when (action) {
            is SketchPadActions.SaveSketch -> saveSketch(action.sketch)
            is SketchPadActions.UpdateSketch -> updateSketch(action.paths)
            SketchPadActions.CheckIfUserIsLoggedIn -> isLoggedIn()
            SketchPadActions.SketchClosed -> _uiState.update { it.copy(sketch = null) }
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
            if (uiState.sketch == null) return@launch
            val updatedSketch = Sketch(
                id = uiState.sketch!!.id,
                name = uiState.sketch!!.name,
                dateCreated = uiState.sketch!!.dateCreated,
                pathList = paths,
                lastModified = Calendar.getInstance().time
            )
            sketchRepository.updateSketch(updatedSketch)

//			if(!uiState.userIsLoggedIn) return@launch
//			updatePathInDb(paths)
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
                    Log.i("Board details on save", response.data.toString())
                    _uiState.update {
                        it.copy(
                            boardDetails = response.data ?: BoardDetails("", "", emptyList()),
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
        if (!response) return@launch
        if (uiState.sketch != null) generateCollabUrl(uiState.sketch!!.id)
        fetchSketchesFromRemoteDB()
    }

    private fun listenForSketchChanges(userId: String, boardId: String) =
        viewModelScope.launch {
            if (!uiState.userIsLoggedIn || !uiState.sketchIsBackedUp) return@launch

            val response = collabRepository.listenForPathChanges(
                userId = userId,
                boardId = boardId
            )
            response.collectLatest { dbResponse ->
                when (dbResponse) {
                    is FirebaseResponse.Success -> {
                        val newPaths = dbResponse.data ?: emptyList()
                        val mergedPaths = _uiState.value.paths + newPaths

                        _uiState.update {
                            it.copy(
                                sketchIsBackedUp = true,
                                error = "",
                                paths = mergedPaths
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

    fun updatePathInDb(paths: List<PathProperties>, userId: String, boardId: String) =
        viewModelScope.launch {
            if (paths.isNotEmpty()) {
                val response =
                    collabRepository.updatePathInDB(
                        userId = userId,
                        boardId = boardId,
                        paths = paths.map { it.toDBPathProperties() }
                    )

                listenForSketchChanges(userId = userId, boardId = boardId)

                if (response is FirebaseResponse.Error) {
                    _uiState.update { it.copy(error = response.message) }
                    _uiEvents.emit(CanvasUiEvents.SnackBarEvent(response.message))
                }
            }
        }

    fun fetchSingleSketch(boardId: String, userId: String) =
        viewModelScope.launch {
            val sketchResponse = collabRepository.fetchSingleSketch(
                userId = userId,
                boardId = boardId
            )

            when (sketchResponse) {
                is FirebaseResponse.Success -> {
                    Log.i("Single sketch from DB", sketchResponse.data.toString())

                    if (sketchResponse.data != null) {
                        _uiState.update {
                            it.copy(
                                sketch = sketchResponse.data,
                                error = ""
                            )
                        }
                    }
                }

                is FirebaseResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            error = sketchResponse.message,
                            sketch = null
                        )
                    }
                }
            }
        }

    //TODO(Handle network interruptions)
//    fun handleReconnection() {
//        listenForSketchChanges()
//        fetchSketch(_uiState.value.boardDetails.boardId)
//    }

    fun generateCollabUrl(boardId: String) =
        viewModelScope.launch {
            Log.i("SketchInfo", "User is logged in: ${uiState.userIsLoggedIn}")
            if (!uiState.userIsLoggedIn) return@launch

            val uri = collabRepository.generateCollabUrl(firebaseUser.uid, boardId)
            Log.i("SketchInfo", "Uri: $uri")
            _uiState.update { it.copy(collabUrl = uri) }
        }

    private suspend fun fetchSketchesFromRemoteDB(): List<Sketch> {
        if (!authRepository.checkIfUserIsLoggedIn()) {
            remoteSketches = emptyList()
            return remoteSketches
        }
        val response = collabRepository.fetchExistingSketches(firebaseUser.uid)

        remoteSketches = when (response) {
            is FirebaseResponse.Success -> {
                response.data ?: emptyList()
            }

            else -> emptyList()
        }
        return remoteSketches
    }


    fun initialize() {
        viewModelScope.launch {
            sketchRepository.getChats().collect {
                _messages.value = emptyList()
                _messages.value = it
                Log.d(TAG, "list of messages during initalization ${messages.value}")
            }
        }
    }
    fun load(){
        viewModelScope.launch {
        sketchRepository.loadChats().collect { messagesList ->
            _messages.value = emptyList()
            _messages.value = messagesList
            Log.d(TAG, "list of messages after creation ${messages.value}")
        }}
    }

    fun onMessageSent() {
        updateTypingStatus( false)
        viewModelScope.launch {
            if (message.isNotEmpty()) {
                sketchRepository.createChats(message,).collect {
                    if (it) {
                        Log.d(TAG, "message sent successfully")
                    } else {
                        Log.d(TAG, "message failed")
                    }
                   /* sketchRepository.loadChats().collect { messagesList ->
                        _messages.value = emptyList()
                        _messages.value = messagesList
                        Log.d(TAG, "list of messages after creation ${messages.value}")
                    }*/

                }
            }

        }

    }
    fun onMessageChange(newValue: String) {
        messageState.value = messageState.value.copy(message = newValue)
      if (newValue.isNotEmpty()){ updateTypingStatus(true) }
        else { updateTypingStatus(false) }
    }
    fun listenForTypingStatuses() {
        viewModelScope.launch {
            sketchRepository.listenForTypingStatuses().collect { users ->
                _typingUsers.value = users
            }
        }
    }
    private fun updateTypingStatus(isTyping: Boolean) {
        viewModelScope.launch {
            sketchRepository.updateTypingStatus( isTyping)
           if (!isTyping) { _typingUsers.value = emptyList() }
        }
    }
}