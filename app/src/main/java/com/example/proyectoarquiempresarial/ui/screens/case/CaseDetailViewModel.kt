package com.example.proyectoarquiempresarial.ui.screens.case

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaseDetailUiState(
    val loading: Boolean = true,
    val collectionCase: CollectionCase? = null,
    val error: String? = null,
    val updating: Boolean = false,
    val updateError: String? = null,
    val selectedStatus: String = "PROMISE"
)

class CaseDetailViewModel(
    private val caseId: String,
    private val repository: CollectionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CaseDetailUiState())
    val state: StateFlow<CaseDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (_state.value.loading && _state.value.collectionCase != null) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.retrieve(caseId)) {
                is ApiResult.Success ->
                    _state.update {
                        it.copy(loading = false, collectionCase = result.data, error = null)
                    }
                is ApiResult.Failure ->
                    _state.update {
                        it.copy(loading = false, error = result.message)
                    }
            }
        }
    }

    fun selectStatus(status: String) {
        _state.update { it.copy(selectedStatus = status, updateError = null) }
    }

    fun updateStatus(status: String) {
        if (_state.value.updating) return
        _state.update { it.copy(updating = true, updateError = null) }
        viewModelScope.launch {
            when (val result = repository.updateStatus(caseId, status)) {
                is ApiResult.Success ->
                    _state.update {
                        it.copy(
                            updating = false,
                            collectionCase = result.data,
                            updateError = null,
                            selectedStatus = status
                        )
                    }
                is ApiResult.Failure ->
                    _state.update {
                        it.copy(updating = false, updateError = result.message)
                    }
            }
        }
    }
}
