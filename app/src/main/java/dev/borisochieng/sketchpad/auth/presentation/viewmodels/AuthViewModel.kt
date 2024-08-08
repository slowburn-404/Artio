package dev.borisochieng.sketchpad.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.auth.presentation.state.UiState
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> get() = _eventFlow.asSharedFlow()


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

}