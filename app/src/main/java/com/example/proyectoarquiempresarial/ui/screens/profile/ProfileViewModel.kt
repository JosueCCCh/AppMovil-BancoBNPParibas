package com.example.proyectoarquiempresarial.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.core.ConfigStore
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.data.auth.AuthRepository
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val apiBaseUrl: String = "",
    val testing: Boolean = false,
    val testResult: String? = null,
    val testSuccess: Boolean? = null,
    val savedMessage: String? = null
)

class ProfileViewModel(
    private val repository: CollectionsRepository,
    private val auth: AuthRepository,
    private val configStore: ConfigStore
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(apiBaseUrl = configStore.apiBaseUrl()))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun saveApiBaseUrl(url: String) {
        configStore.setApiBaseUrl(url)
        _state.update {
            it.copy(
                apiBaseUrl = configStore.apiBaseUrl(),
                savedMessage = "URL guardada correctamente.",
                testResult = null,
                testSuccess = null
            )
        }
    }

    fun testConnection() {
        if (_state.value.testing) return
        _state.update { it.copy(testing = true, testResult = null, testSuccess = null) }
        viewModelScope.launch {
            when (val result = repository.getStrategies()) {
                is ApiResult.Success ->
                    _state.update {
                        it.copy(
                            testing = false,
                            testResult = "Conexión correcta: ${result.data.size} estrategias activas.",
                            testSuccess = true
                        )
                    }
                is ApiResult.Failure ->
                    _state.update {
                        it.copy(
                            testing = false,
                            testResult = result.message,
                            testSuccess = false
                        )
                    }
            }
        }
    }

    fun updateRole(role: UserRole) {
        auth.updateRole(role)
    }

    fun logout() {
        auth.logout()
    }
}
