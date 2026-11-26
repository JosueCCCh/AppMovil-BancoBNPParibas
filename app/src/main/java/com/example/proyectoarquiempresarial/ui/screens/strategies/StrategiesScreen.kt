package com.example.proyectoarquiempresarial.ui.screens.strategies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.Formatters
import com.example.proyectoarquiempresarial.core.Labels
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.ui.components.ActionPill
import com.example.proyectoarquiempresarial.ui.components.EmptyState
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoRow
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.StatusPill

@Composable
fun StrategiesScreen(
    container: AppContainer,
    canRegister: Boolean,
    onRegister: () -> Unit
) {
    val viewModel: StrategiesViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                StrategiesViewModel(container.collectionsRepository)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    ScreenScaffold(
        title = "Estrategias",
        subtitle = "BQ Strategy · Tramos de cobranza",
        actions = {
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
        },
        floatingActionButton = {
            if (canRegister) {
                ExtendedFloatingActionButton(
                    onClick = onRegister,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.error != null) {
                    item { ErrorBanner(message = state.error.orEmpty()) }
                }

                if (!state.loading && state.items.isEmpty() && state.error == null) {
                    item {
                        EmptyState(
                            icon = Icons.Default.List,
                            title = "Sin estrategias activas",
                            message = "Registra un tramo de cobranza para comenzar."
                        )
                    }
                }

                items(state.items, key = { it.strategyId }) { strategy ->
                    StrategyCard(strategy)
                }
            }
        }
    }
}

@Composable
private fun StrategyCard(strategy: Strategy) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = strategy.name.ifBlank { strategy.strategyId },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strategy.strategyId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    StatusPill(strategy.status)
                    Spacer(Modifier.height(4.dp))
                    ActionPill(strategy.action)
                }
            }
            Spacer(Modifier.height(10.dp))
            InfoRow("Tramo", "${strategy.minDays} – ${strategy.maxDays} días")
            InfoRow("Tasa moratoria", Formatters.percent(strategy.lateFeeRate))
            InfoRow("Canal", Labels.channel(strategy.channel))
            InfoRow("Moneda", strategy.currency)
            InfoRow(
                "Vigencia",
                "${Formatters.date(strategy.validFrom)} – ${Formatters.date(strategy.validTo)}"
            )
        }
    }
}
