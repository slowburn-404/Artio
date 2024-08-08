package dev.borisochieng.sketchpad.auth.presentation.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.auth.presentation.state.UiEvent
import dev.borisochieng.sketchpad.auth.presentation.state.UiState
import dev.borisochieng.sketchpad.ui.navigation.AppRoute
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.KeyValueStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthViewModel : ViewModel(), KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val keyValueStore: KeyValueStore by inject()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> get() = _eventFlow.asSharedFlow()

    var startScreen by mutableStateOf(AppRoute.HomeScreen.route); private set

    init {
        checkIfFirstLaunch()
        isLoggedIn()
    }

    private fun checkIfFirstLaunch() {
        viewModelScope.launch {
            keyValueStore.getLaunchStatus().collect {
                startScreen = (if (it) {
                    AppRoute.HomeScreen
                } else AppRoute.OnBoardingScreen).route
            }
        }
    }

    fun saveLaunchStatus() {
        viewModelScope.launch {
            keyValueStore.saveLaunchStatus()
        }
    }


    fun signUpUser(email: String, password: String) =
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = ""
                )
            }
            val response = authRepository.signUp(email, password)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = ""
                )
            }

            when (response) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            user = response.data
                        )
                    }

                }

                is FirebaseResponse.Error -> {
                    val errorMessage: String = response.message
                    val exception: Exception? = response.exception
                    exception?.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }

                    _eventFlow.emit(UiEvent.SnackBarEvent(errorMessage))
                }
            }
        }

    fun loginUser(email: String, password: String) =
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = ""
                )
            }
            val response = authRepository.login(email, password)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = ""
                )
            }

            when (response) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            user = response.data
                        )
                    }

                }

                is FirebaseResponse.Error -> {
                    val errorMessage: String = response.message
                    val exception: Exception? = response.exception
                    exception?.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }

                    _eventFlow.emit(UiEvent.SnackBarEvent(errorMessage))
                }
            }
        }

    fun logoutUser()  =
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = ""
                )
            }
            authRepository.logout()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "",
                    user = null
                )
            }

        }

   private fun isLoggedIn() =
        viewModelScope.launch {
            val response = authRepository.checkIfUserIsLoggedIn()

            when(response) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            user = response.data
                        )
                    }
                }
                is FirebaseResponse.Error -> {
                    _eventFlow.emit(UiEvent.SnackBarEvent(response.message))
                }
            }
        }

    fun updateProfile(imageUrl: Uri, username: String) =
        viewModelScope.launch {
            val updateProfile = authRepository.updateUserProfile(displayName = username, imageUrl = imageUrl)

            when(updateProfile) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            user = updateProfile.data
                        )
                    }
                    _eventFlow.emit(UiEvent.SnackBarEvent("Profile Update Successfully"))
                }

                is FirebaseResponse.Error -> {
                    _eventFlow.emit(UiEvent.SnackBarEvent(updateProfile.message))
                    _uiState.update {
                        it.copy(
                            error = updateProfile.message
                        )
                    }
                }
            }
        }

    fun uploadImage(uri: Uri) =
        viewModelScope.launch {
           authRepository.uploadImageToFireStore(
                uri,
                onUploadFailure = {
                    viewModelScope.launch {
                        _eventFlow.emit(UiEvent.SnackBarEvent("Image uploaded successfully"))
                    }
                },
                onUploadSuccess = {
                    viewModelScope.launch {
                        _eventFlow.emit(UiEvent.SnackBarEvent(it))
                    }
                }
            )


        }

}