package com.example.proyectoarquiempresarial.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoarquiempresarial.BuildConfig
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.ui.components.CollectIqMark
import com.example.proyectoarquiempresarial.ui.components.ErrorBanner
import com.example.proyectoarquiempresarial.ui.components.PrimaryButton
import com.example.proyectoarquiempresarial.ui.components.RequiredLabel
import com.example.proyectoarquiempresarial.ui.components.RoleSelector
import com.example.proyectoarquiempresarial.ui.components.SecondaryButton
import com.example.proyectoarquiempresarial.ui.theme.Green700
import com.example.proyectoarquiempresarial.ui.theme.Green800
import com.example.proyectoarquiempresarial.ui.theme.Green900

@Composable
fun LoginScreen(container: AppContainer) {
    val viewModel: LoginViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                LoginViewModel(container.authRepository, container.configStore)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.GESTOR) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    var apiUrl by rememberSaveable(state.apiBaseUrl) { mutableStateOf(state.apiBaseUrl) }
    var localError by remember { mutableStateOf<String?>(null) }

    val visibleError = localError ?: state.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Green900, Green800, Green700, MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(56.dp))
            CollectIqMark(light = true)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Cobranzas inteligentes sobre AWS serverless",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.82f)
            )
            Spacer(Modifier.height(28.dp))

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (state.challenge) {
                        Text(
                            text = "Cambia tu contraseña",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Cognito solicita reemplazar la contraseña inicial de $username.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                localError = null
                            },
                            label = { Text("Nueva contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localError = null
                            },
                            label = { Text("Confirmar contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Guardar contraseña",
                            loading = state.loading,
                            onClick = {
                                localError = when {
                                    newPassword.length < 8 ->
                                        "La contraseña debe tener al menos 8 caracteres."
                                    newPassword != confirmPassword ->
                                        "Las contraseñas no coinciden."
                                    else -> null
                                }
                                if (localError == null) viewModel.submitNewPassword(newPassword)
                            }
                        )
                        TextButton(
                            onClick = {
                                newPassword = ""
                                confirmPassword = ""
                                localError = null
                                viewModel.cancelChallenge()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Volver al inicio de sesión")
                        }
                    } else {
                        Text(
                            text = "Iniciar sesión",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Accede con tu usuario de Amazon Cognito.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        RoleSelector(
                            selected = role,
                            onSelect = {
                                role = it
                                localError = null
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                localError = null
                            },
                            label = { RequiredLabel("Usuario") },
                            placeholder = {
                                Text(
                                    text = "ej. gestor1",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (username.isBlank()) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sugerencia: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    onClick = {
                                        username = "gestor1"
                                        localError = null
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "gestor1",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                localError = null
                            },
                            label = { RequiredLabel("Contraseña") },
                            placeholder = {
                                Text(
                                    text = "Ingresa tu contraseña",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        text = if (passwordVisible) "Ocultar" else "Ver",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Ingresar",
                            loading = state.loading,
                            onClick = {
                                localError = when {
                                    username.isBlank() || password.isBlank() ->
                                        "Ingresa tu usuario y contraseña."
                                    else -> null
                                }
                                if (localError == null) viewModel.login(username, password, role)
                            }
                        )
                    }

                    if (visibleError != null) {
                        Spacer(Modifier.height(12.dp))
                        ErrorBanner(
                            message = visibleError,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            showAdvanced = !showAdvanced
                            localError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (showAdvanced) "Ocultar configuración" else "Configuración del servidor",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (showAdvanced) {
                        OutlinedTextField(
                            value = apiUrl,
                            onValueChange = {
                                apiUrl = it
                                localError = null
                            },
                            label = { Text("URL base de API Gateway") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Cognito: ${BuildConfig.COGNITO_REGION} · ${BuildConfig.COGNITO_CLIENT_ID}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "User Pool: ${BuildConfig.COGNITO_USER_POOL_ID}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        SecondaryButton(
                            text = "Guardar URL",
                            onClick = {
                                localError = null
                                viewModel.saveApiBaseUrl(apiUrl)
                                apiUrl = container.configStore.apiBaseUrl()
                            }
                        )
                        if (state.configMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.configMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Proyecto de Arquitectura Empresarial · URP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
