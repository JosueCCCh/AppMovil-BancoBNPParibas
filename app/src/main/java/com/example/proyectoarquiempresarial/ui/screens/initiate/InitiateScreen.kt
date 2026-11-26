package com.example.proyectoarquiempresarial.ui.screens.initiate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.ui.components.ActionPill
import com.example.proyectoarquiempresarial.ui.components.DemoDataBar
import com.example.proyectoarquiempresarial.ui.components.DropdownField
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoBanner
import com.example.proyectoarquiempresarial.ui.components.InfoRow
import com.example.proyectoarquiempresarial.ui.components.PrimaryButton
import com.example.proyectoarquiempresarial.ui.components.RequiredLabel
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.SecondaryButton
import com.example.proyectoarquiempresarial.ui.components.StatusPill
import com.example.proyectoarquiempresarial.ui.components.SuccessBanner

@Composable
fun InitiateScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onOpenCase: (String) -> Unit
) {
    val viewModel: InitiateViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                InitiateViewModel(container.collectionsRepository)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result = state.result

    var accountReference by rememberSaveable { mutableStateOf("") }
    var customerReference by rememberSaveable { mutableStateOf("") }
    var overdueAmount by rememberSaveable { mutableStateOf("") }
    var overdueDays by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("PEN") }

    ScreenScaffold(
        title = "Abrir caso",
        subtitle = "Initiate · Control Record",
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
            if (result != null) {
                InitiateResultCard(
                    collectionCase = result,
                    onOpenCase = { onOpenCase(result.caseId) },
                    onReset = {
                        viewModel.reset()
                        accountReference = ""
                        customerReference = ""
                        overdueAmount = ""
                        overdueDays = ""
                        currency = "PEN"
                    }
                )
            } else {
                InfoBanner(
                    message = "La función Lambda selecciona el tramo según los días de mora, " +
                        "calcula el interés moratorio y registra el caso en DynamoDB."
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
                            text = "Datos de la cuenta vencida",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DemoDataBar(
                            onFillDemo = {
                                accountReference = "ACC-00017"
                                customerReference = "PARTY-000123"
                                overdueAmount = "1000"
                                overdueDays = "45"
                                currency = "PEN"
                            },
                            onClear = {
                                accountReference = ""
                                customerReference = ""
                                overdueAmount = ""
                                overdueDays = ""
                            },
                            demoLabel = "Cargar ejemplo (ACC-00017 / 45d)"
                        )

                        OutlinedTextField(
                            value = accountReference,
                            onValueChange = { accountReference = it },
                            label = { RequiredLabel("Cuenta (accountReference)") },
                            placeholder = {
                                Text(
                                    text = "ej. ACC-00017",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            isError = state.fieldErrors.containsKey("accountReference"),
                            supportingText = state.fieldErrors["accountReference"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customerReference,
                            onValueChange = { customerReference = it },
                            label = { Text("Cliente (customerReference)") },
                            placeholder = {
                                Text(
                                    text = "ej. PARTY-000123 (opcional)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = overdueAmount,
                            onValueChange = { overdueAmount = it },
                            label = { RequiredLabel("Monto vencido") },
                            placeholder = {
                                Text(
                                    text = "ej. 1000.00",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = state.fieldErrors.containsKey("overdueAmount"),
                            supportingText = state.fieldErrors["overdueAmount"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = overdueDays,
                            onValueChange = { overdueDays = it },
                            label = { RequiredLabel("Días de mora") },
                            placeholder = {
                                Text(
                                    text = "ej. 45",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.fieldErrors.containsKey("overdueDays"),
                            supportingText = state.fieldErrors["overdueDays"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownField(
                            label = "Moneda",
                            value = currency,
                            options = Labels.currencies.map { it to it },
                            onSelected = { currency = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.error != null) {
                    ErrorBanner(message = state.error.orEmpty())
                }

                PrimaryButton(
                    text = "Abrir caso y calcular moratorio",
                    loading = state.loading,
                    onClick = {
                        viewModel.submit(
                            accountReference = accountReference,
                            customerReference = customerReference,
                            currency = currency,
                            overdueAmount = overdueAmount,
                            overdueDays = overdueDays
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InitiateResultCard(
    collectionCase: CollectionCase,
    onOpenCase: () -> Unit,
    onReset: () -> Unit
) {
    SuccessBanner(message = "Caso de cobranza abierto correctamente.")

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Expediente generado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = collectionCase.caseId,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(collectionCase.status)
                ActionPill(collectionCase.action)
            }
            Spacer(Modifier.height(10.dp))

            InfoRow("Cuenta", collectionCase.accountReference)
            InfoRow("Cliente", collectionCase.customerReference.ifBlank { "—" })
            InfoRow("Tramo", collectionCase.bucket.ifBlank { "—" })
            InfoRow("Estrategia", collectionCase.strategyId.ifBlank { "—" })
            InfoRow("Acción sugerida", Labels.action(collectionCase.action))
            InfoRow("Días de mora", Formatters.days(collectionCase.overdueDays))
            InfoRow("Monto vencido", Formatters.money(collectionCase.overdueAmount, collectionCase.currency))
            InfoRow("Interés moratorio", Formatters.money(collectionCase.lateFee, collectionCase.currency))
            InfoRow(
                label = "Total exigible",
                value = Formatters.money(collectionCase.totalDue, collectionCase.currency),
                emphasize = true
            )
            InfoRow("Estado", Labels.status(collectionCase.status))
        }
    }

    PrimaryButton(
        text = "Ver detalle del caso",
        onClick = onOpenCase
    )
    SecondaryButton(
        text = "Abrir otro caso",
        onClick = onReset
    )
    Spacer(Modifier.height(16.dp))
}
