package com.example.proyectoarquiempresarial.ui.screens.strategies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StrategiesUiState(
    val loading: Boolean = true,
    val items: List<Strategy> = emptyList(),
    val error: String? = null
)

class StrategiesViewModel(
    private val repository: CollectionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StrategiesUiState())
    val state: StateFlow<StrategiesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (_state.value.loading && _state.value.items.isNotEmpty()) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.getStrategies()) {
                is ApiResult.Success ->
                    _state.update {
                        it.copy(loading = false, items = result.data, error = null)
                    }
                is ApiResult.Failure ->
                    _state.update {
                        it.copy(loading = false, error = result.message)
                    }
            }
        }
    }
}
