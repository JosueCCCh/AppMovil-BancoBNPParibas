package com.example.proyectoarquiempresarial.ui.screens.strategies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoarquiempresarial.core.Validators
import com.example.proyectoarquiempresarial.data.model.RegisterStrategyRequest
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterStrategyUiState(
    val loading: Boolean = false,
    val created: Strategy? = null,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class RegisterStrategyViewModel(
    private val repository: CollectionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterStrategyUiState())
    val state: StateFlow<RegisterStrategyUiState> = _state.asStateFlow()

    fun submit(
        strategyId: String,
        name: String,
        minDays: String,
        maxDays: String,
        action: String,
        lateFeeRate: String,
        channel: String,
        currency: String,
        validFrom: String,
        validTo: String
    ) {
        if (_state.value.loading) return
        val errors = Validators.strategy(
            strategyId = strategyId,
            name = name,
            minDays = minDays,
            maxDays = maxDays,
            lateFeeRate = lateFeeRate,
            validFrom = validFrom,
            validTo = validTo
        )
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, error = null) }
            return
        }

        val request = RegisterStrategyRequest(
            strategyId = strategyId.trim(),
            name = name.trim(),
            minDays = minDays.trim().toInt(),
            maxDays = maxDays.trim().toInt(),
            action = action,
            lateFeeRate = lateFeeRate.trim().replace(',', '.').toDouble(),
            channel = channel,
            currency = currency,
            validFrom = validFrom.trim(),
            validTo = validTo.trim()
        )

        _state.update { it.copy(loading = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            when (val result = repository.registerStrategy(request)) {
                is ApiResult.Success ->
                    _state.update { it.copy(loading = false, created = result.data, error = null) }
                is ApiResult.Failure ->
                    _state.update { it.copy(loading = false, error = result.message) }
            }
        }
    }

    fun reset() {
        _state.value = RegisterStrategyUiState()
    }
}
