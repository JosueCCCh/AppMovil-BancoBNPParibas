package com.example.proyectoarquiempresarial.ui.screens.initiate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.core.Validators
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.data.model.InitiateRequest
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InitiateUiState(
    val loading: Boolean = false,
    val result: CollectionCase? = null,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class InitiateViewModel(
    private val repository: CollectionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InitiateUiState())
    val state: StateFlow<InitiateUiState> = _state.asStateFlow()

    fun submit(
        accountReference: String,
        customerReference: String,
        currency: String,
        overdueAmount: String,
        overdueDays: String
    ) {
        if (_state.value.loading) return
        val errors = Validators.initiate(accountReference, overdueAmount, overdueDays)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, error = null) }
            return
        }

        val amount = overdueAmount.trim().replace(',', '.').toDouble()
        val days = overdueDays.trim().toInt()
        _state.update { it.copy(loading = true, error = null, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            val request = InitiateRequest(
                accountReference = accountReference.trim(),
                customerReference = customerReference.trim(),
                currency = currency,
                overdueAmount = amount,
                overdueDays = days
            )
            when (val result = repository.initiate(request)) {
                is ApiResult.Success ->
                    _state.update { it.copy(loading = false, result = result.data, error = null) }
                is ApiResult.Failure ->
                    _state.update { it.copy(loading = false, error = result.message) }
            }
        }
    }

    fun reset() {
        _state.value = InitiateUiState()
    }
}
