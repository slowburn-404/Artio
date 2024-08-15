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

    var startScreen by mutableStateOf(
        AppRoute.HomeScreen.route
    ); private set

    init {
        checkIfFirstLaunch()
        isLoggedIn()
    }

    private fun checkIfFirstLaunch() {
        viewModelScope.launch {
            keyValueStore.getLaunchStatus().collect { hasFinishedOnboarding ->
//                startScreen = when {
//                    !hasFinishedOnboarding -> AppRoute.OnBoardingScreen
////                    !isLoggedIn -> AppRoute.SignUpScreen // this causes the screen to pop up on every launch, which isn't good
//                    else -> AppRoute.HomeScreen
                startScreen = if(hasFinishedOnboarding) {
                    AppRoute.HomeScreen
                } else {
                    AppRoute.OnBoardingScreen
                }.route
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
            val response = authRepository.signUp(email.trim(), password)

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
                            user = response.data,
                            error = "",
                            isLoggedIn = true
                        )
                    }

                    _eventFlow.emit(UiEvent.SnackBarEvent("Account created successfully"))

                }

                is FirebaseResponse.Error -> {
                    val errorMessage: String = response.message
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage,
                            isLoggedIn = false
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
            val response = authRepository.login(email.trim(), password)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "",
                )
            }

            when (response) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            user = response.data,
                            error = "",
                            isLoggedIn = true
                        )
                    }
                    _eventFlow.emit(UiEvent.SnackBarEvent("Log in successful"))

                }

                is FirebaseResponse.Error -> {
                    val errorMessage: String = response.message

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage,
                            isLoggedIn = false
                        )
                    }
                    _eventFlow.emit(UiEvent.SnackBarEvent(errorMessage))
                }
            }
        }

    fun logoutUser() =
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
                    user = null,
                    isLoggedIn = false
                )
            }

        }

    private fun isLoggedIn() =
        viewModelScope.launch {
            val response = authRepository.checkIfUserIsLoggedIn()

            _uiState.update {
                it.copy(
                    isLoggedIn = response
                )
            }
        }

//    fun updateProfile(imageUrl: Uri, username: String) =
//        viewModelScope.launch {
//            val updateProfileTask =
//                authRepository.updateUserProfile(displayName = username, imageUrl = imageUrl)
//
//            when (updateProfileTask) {
//                is FirebaseResponse.Success -> {
//                    _uiState.update {
//                        it.copy(
//                            user = updateProfileTask.data
//                        )
//                    }
//                    _eventFlow.emit(UiEvent.SnackBarEvent("Profile Update Successfully"))
//                }
//
//                is FirebaseResponse.Error -> {
//                    _eventFlow.emit(UiEvent.SnackBarEvent(updateProfileTask.message))
//                    _uiState.update {
//                        it.copy(
//                            error = updateProfileTask.message
//                        )
//                    }
//                }
//            }
//        }
//
//    fun uploadImage(uri: Uri) =
//        viewModelScope.launch {
//            val uploadTask = authRepository.uploadImageToFireStore(uri = uri)
//
//            when (uploadTask) {
//                is FirebaseResponse.Success -> {
//                    val imageUrl = uploadTask.data
//
//                    if (imageUrl != null) {
//                        _uiState.update {
//                            it.copy(
//                                user = it.user?.copy(
//                                    imageUrl = imageUrl
//                                )
//                            )
//                        }
//                    }
//                }
//
//                is FirebaseResponse.Error -> {
//                    _uiState.update {
//                        it.copy(
//                            error = uploadTask.message
//                        )
//                    }
//                    _eventFlow.emit(UiEvent.SnackBarEvent(uploadTask.message))
//                }
//            }
//        }

    fun uploadImageAndUpdateProfile(uri: Uri, username: String) =
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = ""
                )
            }
            // Upload the image and get the download URL
            val uploadImageTask = authRepository.uploadImageToFireStore(uri = uri)

            when (uploadImageTask) {
                is FirebaseResponse.Success -> {
                    val imageUrl = uploadImageTask.data

                    //Update the user profile with the new image URL and username

                    if (imageUrl != null) {
                        val updateProfileTask = authRepository.updateUserProfile(
                            displayName = username,
                            imageUrl = imageUrl
                        )

                        when (updateProfileTask) {
                            is FirebaseResponse.Success -> {
                                _uiState.update {
                                    it.copy(
                                        user = updateProfileTask.data,
                                        error = "",
                                        isLoading = false
                                    )
                                }
                                _eventFlow.emit(UiEvent.SnackBarEvent("Profile updated successfully"))
                            }

                            is FirebaseResponse.Error -> {
                                _eventFlow.emit(UiEvent.SnackBarEvent(updateProfileTask.message))
                                _uiState.update {
                                    it.copy(
                                        error = updateProfileTask.message,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }

                is FirebaseResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            error = uploadImageTask.message,
                            isLoading = false
                        )
                    }
                    _eventFlow.emit(UiEvent.SnackBarEvent(uploadImageTask.message))
                }
            }

        }
}