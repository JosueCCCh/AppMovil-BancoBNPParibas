package com.example.proyectoarquiempresarial.ui.screens.strategies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.Formatters
import com.example.proyectoarquiempresarial.core.Labels
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.ui.components.ActionPill
import com.example.proyectoarquiempresarial.ui.components.DemoDataBar
import com.example.proyectoarquiempresarial.ui.components.DropdownField
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoBanner
import com.example.proyectoarquiempresarial.ui.components.InfoRow
import com.example.proyectoarquiempresarial.ui.components.PrimaryButton
import com.example.proyectoarquiempresarial.ui.components.RequiredLabel
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.StatusPill
import com.example.proyectoarquiempresarial.ui.components.SuccessBanner

@Composable
fun RegisterStrategyScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val viewModel: RegisterStrategyViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RegisterStrategyViewModel(container.collectionsRepository)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val created = state.created

    var strategyId by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var minDays by rememberSaveable { mutableStateOf("") }
    var maxDays by rememberSaveable { mutableStateOf("") }
    var action by rememberSaveable { mutableStateOf("LEGAL") }
    var lateFeeRate by rememberSaveable { mutableStateOf("") }
    var channel by rememberSaveable { mutableStateOf("LEGAL") }
    var currency by rememberSaveable { mutableStateOf("PEN") }
    var validFrom by rememberSaveable { mutableStateOf("") }
    var validTo by rememberSaveable { mutableStateOf("") }

    ScreenScaffold(
        title = "Registrar estrategia",
        subtitle = "Register · BQ Strategy",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (created != null) {
                StrategyCreatedCard(
                    strategy = created,
                    onBack = {
                        viewModel.reset()
                        onBack()
                    }
                )
            } else {
                InfoBanner(
                    message = "La estrategia se registra con estado ACTIVE. " +
                        "Si el identificador ya existe, API Gateway/Lambda responde 409 Conflict."
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Configuración del tramo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DemoDataBar(
                            onFillDemo = {
                                strategyId = "STR-005"
                                name = "Tramo 121+ días"
                                minDays = "121"
                                maxDays = "9999"
                                action = "LEGAL"
                                lateFeeRate = "10"
                                channel = "LEGAL"
                                currency = "PEN"
                                validFrom = "2026-01-01"
                                validTo = "2026-12-31"
                            },
                            onClear = {
                                strategyId = ""
                                name = ""
                                minDays = ""
                                maxDays = ""
                                lateFeeRate = ""
                                validFrom = ""
                                validTo = ""
                            },
                            demoLabel = "Cargar ejemplo (STR-005)"
                        )

                        OutlinedTextField(
                            value = strategyId,
                            onValueChange = { strategyId = it },
                            label = { RequiredLabel("strategyId") },
                            placeholder = {
                                Text(
                                    text = "ej. STR-005",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            isError = state.fieldErrors.containsKey("strategyId"),
                            supportingText = state.fieldErrors["strategyId"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { RequiredLabel("Nombre del tramo") },
                            placeholder = {
                                Text(
                                    text = "ej. Tramo 121+ días",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            isError = state.fieldErrors.containsKey("name"),
                            supportingText = state.fieldErrors["name"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = minDays,
                            onValueChange = { minDays = it },
                            label = { RequiredLabel("Día mínimo") },
                            placeholder = {
                                Text(
                                    text = "ej. 121",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.fieldErrors.containsKey("minDays"),
                            supportingText = state.fieldErrors["minDays"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = maxDays,
                            onValueChange = { maxDays = it },
                            label = { RequiredLabel("Día máximo") },
                            placeholder = {
                                Text(
                                    text = "ej. 9999",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.fieldErrors.containsKey("maxDays"),
                            supportingText = state.fieldErrors["maxDays"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownField(
                            label = "Acción BIAN",
                            value = action,
                            options = Labels.actions.map { it to Labels.action(it) },
                            onSelected = { action = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lateFeeRate,
                            onValueChange = { lateFeeRate = it },
                            label = { RequiredLabel("Tasa moratoria (%)") },
                            placeholder = {
                                Text(
                                    text = "ej. 10",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = state.fieldErrors.containsKey("lateFeeRate"),
                            supportingText = state.fieldErrors["lateFeeRate"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownField(
                            label = "Canal",
                            value = channel,
                            options = Labels.channels.map { it to Labels.channel(it) },
                            onSelected = { channel = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownField(
                            label = "Moneda",
                            value = currency,
                            options = Labels.currencies.map { it to it },
                            onSelected = { currency = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = validFrom,
                            onValueChange = { validFrom = it },
                            label = { RequiredLabel("Vigente desde (AAAA-MM-DD)") },
                            placeholder = {
                                Text(
                                    text = "ej. 2026-01-01",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            isError = state.fieldErrors.containsKey("validFrom"),
                            supportingText = state.fieldErrors["validFrom"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = validTo,
                            onValueChange = { validTo = it },
                            label = { RequiredLabel("Vigente hasta (AAAA-MM-DD)") },
                            placeholder = {
                                Text(
                                    text = "ej. 2026-12-31",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            isError = state.fieldErrors.containsKey("validTo"),
                            supportingText = state.fieldErrors["validTo"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.error != null) {
                    ErrorBanner(message = state.error.orEmpty())
                }

                PrimaryButton(
                    text = "Registrar estrategia",
                    loading = state.loading,
                    onClick = {
                        viewModel.submit(
                            strategyId = strategyId,
                            name = name,
                            minDays = minDays,
                            maxDays = maxDays,
                            action = action,
                            lateFeeRate = lateFeeRate,
                            channel = channel,
                            currency = currency,
                            validFrom = validFrom,
                            validTo = validTo
                        )
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StrategyCreatedCard(strategy: Strategy, onBack: () -> Unit) {
    SuccessBanner(message = "Estrategia registrada y activa en DynamoDB.")
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = strategy.strategyId,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strategy.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(strategy.status)
                ActionPill(strategy.action)
            }
            Spacer(Modifier.height(10.dp))
            InfoRow("Tramo", "${strategy.minDays} – ${strategy.maxDays} días")
            InfoRow("Tasa", Formatters.percent(strategy.lateFeeRate))
            InfoRow("Canal", Labels.channel(strategy.channel))
            InfoRow("Moneda", strategy.currency)
            InfoRow("Vigencia", "${Formatters.date(strategy.validFrom)} – ${Formatters.date(strategy.validTo)}")
        }
    }
    PrimaryButton(text = "Volver a estrategias", onClick = onBack)
}
