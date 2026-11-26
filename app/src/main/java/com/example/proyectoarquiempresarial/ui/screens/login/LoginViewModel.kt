package com.example.proyectoarquiempresarial.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.core.ConfigStore
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.data.auth.AuthRepository
import com.example.proyectoarquiempresarial.data.auth.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val challenge: Boolean = false,
    val apiBaseUrl: String = "",
    val configMessage: String? = null
)

class LoginViewModel(
    private val auth: AuthRepository,
    private val configStore: ConfigStore
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState(apiBaseUrl = configStore.apiBaseUrl()))
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private var pendingUsername: String? = null
    private var pendingChallengeSession: String? = null
    private var pendingRole: UserRole = UserRole.GESTOR

    fun login(username: String, password: String, role: UserRole) {
        if (_state.value.loading) return
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Ingresa tu usuario y contraseña.") }
            return
        }
        _state.update { it.copy(loading = true, error = null, configMessage = null) }
        viewModelScope.launch {
            when (val result = auth.login(username, password, role)) {
                is LoginResult.Success -> {
                    pendingUsername = null
                    pendingChallengeSession = null
                    _state.update { it.copy(loading = false) }
                }
                is LoginResult.NewPasswordRequired -> {
                    pendingUsername = username.trim()
                    pendingChallengeSession = result.session
                    pendingRole = role
                    _state.update {
                        it.copy(
                            loading = false,
                            challenge = true,
                            error = null
                        )
                    }
                }
                is LoginResult.Failure -> {
                    _state.update { it.copy(loading = false, error = result.message) }
                }
            }
        }
    }

    fun submitNewPassword(newPassword: String) {
        if (_state.value.loading) return
        val username = pendingUsername ?: run {
            _state.update { it.copy(error = "La sesión de cambio de contraseña expiró.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (
                val result = auth.completeNewPassword(
                    username = username,
                    newPassword = newPassword,
                    challengeSession = pendingChallengeSession,
                    role = pendingRole
                )
            ) {
                is LoginResult.Success -> {
                    pendingUsername = null
                    pendingChallengeSession = null
                    _state.update { it.copy(loading = false, challenge = false) }
                }
                is LoginResult.NewPasswordRequired ->
                    _state.update {
                        it.copy(loading = false, error = "Cognito requiere otro paso de autenticación.")
                    }
                is LoginResult.Failure ->
                    _state.update { it.copy(loading = false, error = result.message) }
            }
        }
    }

    fun saveApiBaseUrl(url: String) {
        configStore.setApiBaseUrl(url)
        _state.update {
            it.copy(
                apiBaseUrl = configStore.apiBaseUrl(),
                configMessage = "URL del servicio actualizada.",
                error = null
            )
        }
    }

    fun cancelChallenge() {
        pendingUsername = null
        pendingChallengeSession = null
        _state.update { it.copy(challenge = false, error = null, loading = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
