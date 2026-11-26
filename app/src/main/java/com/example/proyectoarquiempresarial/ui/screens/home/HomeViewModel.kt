package com.example.proyectoarquiempresarial.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.data.local.RecentCasesStore
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val strategyCount: Int? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repository: CollectionsRepository,
    recentCasesStore: RecentCasesStore
) : ViewModel() {

    val recentCases = recentCasesStore.items

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.getStrategies()) {
                is ApiResult.Success ->
                    _state.update {
                        it.copy(loading = false, strategyCount = result.data.size, error = null)
                    }
                is ApiResult.Failure ->
                    _state.update {
                        it.copy(loading = false, error = result.message)
                    }
            }
        }
    }
}
