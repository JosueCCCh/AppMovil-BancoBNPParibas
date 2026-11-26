package com.example.proyectoarquiempresarial.ui.screens.case

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.Formatters
import com.example.proyectoarquiempresarial.core.Labels
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.ui.components.ActionPill
import com.example.proyectoarquiempresarial.ui.components.DropdownField
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoBanner
import com.example.proyectoarquiempresarial.ui.components.InfoRow
import com.example.proyectoarquiempresarial.ui.components.PrimaryButton
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.StatusPill

@Composable
fun CaseDetailScreen(
    container: AppContainer,
    caseId: String,
    role: UserRole,
    onBack: () -> Unit
) {
    val viewModel: CaseDetailViewModel = viewModel(
        key = caseId,
        factory = viewModelFactory {
            initializer {
                CaseDetailViewModel(caseId, container.collectionsRepository)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "Detalle del caso",
        subtitle = caseId,
        onBack = onBack,
        actions = {
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.loading && state.collectionCase == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null && state.collectionCase == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ErrorBanner(message = state.error.orEmpty())
                        PrimaryButton(text = "Reintentar", onClick = viewModel::load)
                    }
                }
                state.collectionCase != null -> {
                    val collectionCase = state.collectionCase!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            CaseHeaderCard(collectionCase)
                        }
                        item {
                            FinancialCard(collectionCase)
                        }
                        item {
                            CaseDataCard(collectionCase)
                        }
                        item {
                            UpdateCard(
                                collectionCase = collectionCase,
                                role = role,
                                selectedStatus = state.selectedStatus,
                                updating = state.updating,
                                updateError = state.updateError,
                                onSelectStatus = viewModel::selectStatus,
                                onUpdate = viewModel::updateStatus
                            )
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaseHeaderCard(collectionCase: CollectionCase) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = "Expediente de cobranza",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = collectionCase.caseId,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(collectionCase.status)
                ActionPill(collectionCase.action)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = Formatters.money(collectionCase.totalDue, collectionCase.currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Total exigible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun FinancialCard(collectionCase: CollectionCase) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Resumen financiero", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            InfoRow(
                "Monto vencido",
                Formatters.money(collectionCase.overdueAmount, collectionCase.currency)
            )
            InfoRow(
                "Interés moratorio",
                Formatters.money(collectionCase.lateFee, collectionCase.currency)
            )
            InfoRow(
                "Total exigible",
                Formatters.money(collectionCase.totalDue, collectionCase.currency),
                emphasize = true
            )
            InfoRow("Moneda", collectionCase.currency)
        }
    }
}

@Composable
private fun CaseDataCard(collectionCase: CollectionCase) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Datos del caso", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            InfoRow("Cuenta", collectionCase.accountReference)
            InfoRow("Cliente", collectionCase.customerReference.ifBlank { "—" })
            InfoRow("Días de mora", Formatters.days(collectionCase.overdueDays))
            InfoRow("Tramo", collectionCase.bucket.ifBlank { "—" })
            InfoRow("Estrategia", collectionCase.strategyId.ifBlank { "—" })
            InfoRow("Acción", Labels.action(collectionCase.action))
            InfoRow("Creado", Formatters.dateTime(collectionCase.createdAt))
            InfoRow("Actualizado", Formatters.dateTime(collectionCase.updatedAt))
        }
    }
}

@Composable
private fun UpdateCard(
    collectionCase: CollectionCase,
    role: UserRole,
    selectedStatus: String,
    updating: Boolean,
    updateError: String?,
    onSelectStatus: (String) -> Unit,
    onUpdate: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Gestión del caso", style = MaterialTheme.typography.titleMedium)

            if (collectionCase.status.uppercase() != "OPEN") {
                InfoBanner(
                    message = "El caso está en estado ${Labels.status(collectionCase.status)}. " +
                        "Solo se puede actualizar mientras esté OPEN."
                )
            } else {
                if (role == UserRole.GESTOR) {
                    DropdownField(
                        label = "Nuevo estado",
                        value = selectedStatus,
                        options = Labels.updateStatuses(role).map { it to Labels.status(it) },
                        onSelected = onSelectStatus,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrimaryButton(
                        text = "Actualizar estado",
                        loading = updating,
                        onClick = { onUpdate(selectedStatus) }
                    )
                } else {
                    Text(
                        text = "Si llegaste a un acuerdo con el banco, registra tu promesa de pago.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PrimaryButton(
                        text = "Registrar promesa de pago",
                        loading = updating,
                        onClick = { onUpdate("PROMISE") }
                    )
                }
            }

            if (updateError != null) {
                ErrorBanner(message = updateError)
            }
        }
    }
}
