package com.example.proyectoarquiempresarial.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.ui.components.ActionTile
import com.example.proyectoarquiempresarial.ui.components.CaseCard
import com.example.proyectoarquiempresarial.ui.components.CaseLookupDialog
import com.example.proyectoarquiempresarial.ui.components.EmptyState
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoBanner
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.SectionTitle
import com.example.proyectoarquiempresarial.ui.theme.Green700
import com.example.proyectoarquiempresarial.ui.theme.Green900

@Composable
fun HomeScreen(
    container: AppContainer,
    role: UserRole,
    username: String,
    onNewCase: () -> Unit,
    onOpenCase: (String) -> Unit,
    onStrategies: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(container.collectionsRepository, container.recentCasesStore)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recentCases by viewModel.recentCases.collectAsStateWithLifecycle()
    var showLookup by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    ScreenScaffold(
        title = "Inicio",
        subtitle = if (role == UserRole.GESTOR) "Panel de cobranzas" else "Autoservicio del cliente",
        actions = {
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HeroCard(
                        role = role,
                        username = username,
                        strategyCount = state.strategyCount,
                        recentCount = recentCases.size
                    )
                }

                if (state.error != null) {
                    item {
                        ErrorBanner(message = state.error.orEmpty())
                    }
                }

                if (role == UserRole.CLIENTE) {
                    item {
                        InfoBanner(
                            message = "Consulta tu caso con el identificador que te entregó el banco. " +
                                "Desde el detalle podrás registrar una promesa de pago."
                        )
                    }
                }

                item {
                    SectionTitle("Accesos rápidos")
                }

                if (role == UserRole.GESTOR) {
                    item {
                        ActionTile(
                            icon = Icons.Default.Add,
                            title = "Abrir caso",
                            subtitle = "Calcula el moratorio y registra la cobranza",
                            onClick = onNewCase
                        )
                    }
                    item {
                        ActionTile(
                            icon = Icons.Default.Search,
                            title = "Consultar caso",
                            subtitle = "Busca un expediente por su caseId",
                            onClick = { showLookup = true }
                        )
                    }
                    item {
                        ActionTile(
                            icon = Icons.Default.List,
                            title = "Estrategias",
                            subtitle = "Tramos, tasas y canales activos",
                            onClick = onStrategies
                        )
                    }
                } else {
                    item {
                        ActionTile(
                            icon = Icons.Default.Search,
                            title = "Consultar mi caso",
                            subtitle = "Ingresa el caseId de tu cobranza",
                            onClick = { showLookup = true }
                        )
                    }
                }

                item {
                    SectionTitle("Casos recientes")
                }

                if (recentCases.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.List,
                            title = "Aún no hay casos",
                            message = if (role == UserRole.GESTOR) {
                                "Abre un caso de cobranza para verlo aquí."
                            } else {
                                "Consulta un caso para guardarlo en este dispositivo."
                            }
                        )
                    }
                } else {
                    items(recentCases, key = { it.caseId }) { case ->
                        CaseCard(case = case, onClick = { onOpenCase(case.caseId) })
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showLookup) {
        CaseLookupDialog(
            onDismiss = { showLookup = false },
            onConfirm = { caseId ->
                showLookup = false
                if (caseId.isNotBlank()) onOpenCase(caseId)
            }
        )
    }
}

@Composable
private fun HeroCard(
    role: UserRole,
    username: String,
    strategyCount: Int?,
    recentCount: Int
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Green700, Green900)))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = if (role == UserRole.GESTOR) "Panel de cobranzas" else "Mi cobranza",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    HeroMetric(
                        label = "Estrategias activas",
                        value = strategyCount?.toString() ?: "—"
                    )
                    HeroMetric(
                        label = "Casos recientes",
                        value = recentCount.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
