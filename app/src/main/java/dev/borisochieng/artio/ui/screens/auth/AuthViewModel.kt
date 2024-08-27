package dev.borisochieng.artio.ui.screens.auth

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.artio.auth.data.FirebaseResponse
import dev.borisochieng.artio.auth.domain.AuthRepository
import dev.borisochieng.artio.auth.domain.model.User
import dev.borisochieng.artio.database.KeyValueStore
import dev.borisochieng.artio.ui.navigation.AppRoute
import dev.borisochieng.artio.ui.screens.auth.state.UiEvent
import dev.borisochieng.artio.ui.screens.auth.state.UiState
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

    fun refreshUserData() =
        viewModelScope.launch {
            val updatedUser = FirebaseAuth.getInstance().currentUser
            updatedUser?.let { currentUser ->
                val user = User(
                    uid = currentUser.uid,
                    email = currentUser.email!!,
                    displayName = currentUser.displayName,
                    imageUrl = currentUser.photoUrl,
                    isLoggedIn = true

                    )
                _uiState.update {
                    it.copy(
                        user = user
                    )
                }
            }
        }

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
                                Log.i("User profile", "Updated user: ${updateProfileTask.data}")
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

    fun resetPassword(email: String) =
        viewModelScope.launch {
            val sendEmailTask = authRepository.sendPasswordResetEmail(email)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = ""
                )
            }

            when(sendEmailTask) {
                is FirebaseResponse.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    _eventFlow.emit(UiEvent.SnackBarEvent(sendEmailTask.data.toString()))
                }

                is FirebaseResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            error = sendEmailTask.message,
                            isLoading = false
                        )
                    }

                    _eventFlow.emit(UiEvent.SnackBarEvent(sendEmailTask.message))
                }
            }
        }
}