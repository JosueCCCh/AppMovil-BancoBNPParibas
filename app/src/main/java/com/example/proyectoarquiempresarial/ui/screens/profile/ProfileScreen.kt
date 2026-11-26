package com.example.proyectoarquiempresarial.ui.screens.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.BuildConfig
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.Session
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.InfoRow
import com.example.proyectoarquiempresarial.ui.components.PrimaryButton
import com.example.proyectoarquiempresarial.ui.components.RoleSelector
import com.example.proyectoarquiempresarial.ui.components.ScreenScaffold
import com.example.proyectoarquiempresarial.ui.components.SectionTitle
import com.example.proyectoarquiempresarial.ui.components.SuccessBanner
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    container: AppContainer,
    session: Session,
    onRoleChanged: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    repository = container.collectionsRepository,
                    auth = container.authRepository,
                    configStore = container.configStore
                )
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var remainingMinutes by remember(session.expiresAtEpochSeconds) {
        mutableStateOf(session.remainingMinutes())
    }
    var apiUrl by rememberSaveable(state.apiBaseUrl) { mutableStateOf(state.apiBaseUrl) }

    LaunchedEffect(session.expiresAtEpochSeconds) {
        while (true) {
            remainingMinutes = session.remainingMinutes()
            delay(30_000L)
        }
    }

    ScreenScaffold(
        title = "Perfil",
        subtitle = "Cuenta y configuración"
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = session.username,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Sesión autenticada con Amazon Cognito",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        InfoRow("Rol activo", session.role.label)
                        InfoRow("Token válido por", "$remainingMinutes min")
                        InfoRow("Tipo de token", "IdToken (JWT)")
                        Spacer(Modifier.height(12.dp))
                        SectionTitle("Cambiar experiencia")
                        Spacer(Modifier.height(8.dp))
                        RoleSelector(
                            selected = session.role,
                            onSelect = { role ->
                                if (role != session.role) {
                                    viewModel.updateRole(role)
                                    onRoleChanged()
                                }
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Servicio de cobranzas", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = apiUrl,
                            onValueChange = { apiUrl = it },
                            label = { Text("URL base de API Gateway") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.saveApiBaseUrl(apiUrl) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Guardar")
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.saveApiBaseUrl(apiUrl)
                                    viewModel.testConnection()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Probar")
                            }
                        }
                        if (state.savedMessage != null) {
                            Text(
                                text = state.savedMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (state.testing) {
                            Text(
                                text = "Probando conexión…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.testResult != null) {
                            if (state.testSuccess == true) {
                                SuccessBanner(message = state.testResult.orEmpty())
                            } else {
                                ErrorBanner(message = state.testResult.orEmpty())
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Infraestructura AWS", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        InfoRow("Región", BuildConfig.COGNITO_REGION)
                        InfoRow("User Pool", BuildConfig.COGNITO_USER_POOL_ID)
                        InfoRow("App Client", BuildConfig.COGNITO_CLIENT_ID)
                        InfoRow("Autorizador", "cognito-collections-authorizer")
                        InfoRow("Etapa API", "dev")
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Aplicación", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        InfoRow("Versión", BuildConfig.VERSION_NAME)
                        InfoRow("Estándar", "BIAN Service Domain Collections")
                        InfoRow("Backend", "API Gateway · Lambda · DynamoDB")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = viewModel::logout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Text("  Salir de CollectIQ")
                }
            }

            item {
                Text(
                    text = "CollectIQ · Proyecto de Arquitectura Empresarial · URP 2026-II",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
