package com.posopensrc.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.AuthRepository
import com.posopensrc.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    _loginResult.value = LoginResult.AlreadyLoggedIn
                }
            }
        }
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 6) {
            _uiState.value = _uiState.value.copy(pin = pin, error = null)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun toggleLoginMethod() {
        _uiState.value = _uiState.value.copy(
            usePin = !_uiState.value.usePin,
            pin = "",
            password = "",
            error = null
        )
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Username harus diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val user = if (state.usePin) {
                if (state.pin.isBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "PIN harus diisi")
                    return@launch
                }
                authRepository.verifyPin(state.username, state.pin)
            } else {
                if (state.password.isBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Password harus diisi")
                    return@launch
                }
                authRepository.verifyPassword(state.username, state.password)
            }

            if (user != null) {
                sessionManager.saveSession(
                    userId = user.id,
                    username = user.username,
                    fullName = user.fullName,
                    role = user.role
                )
                _loginResult.value = LoginResult.Success(user)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Username atau ${if (state.usePin) "PIN" else "Password"} salah"
                )
            }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val pin: String = "",
    val password: String = "",
    val usePin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginResult {
    data object Idle : LoginResult()
    data object AlreadyLoggedIn : LoginResult()
    data class Success(val user: User) : LoginResult()
}
