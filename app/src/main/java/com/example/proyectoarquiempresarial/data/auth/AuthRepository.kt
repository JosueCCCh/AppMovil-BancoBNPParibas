package com.example.proyectoarquiempresarial.data.auth

import com.example.proyectoarquiempresarial.core.ConfigStore
import com.example.proyectoarquiempresarial.core.Session
import com.example.proyectoarquiempresarial.core.SessionStore
import com.example.proyectoarquiempresarial.core.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface LoginResult {
    data object Success : LoginResult
    data class NewPasswordRequired(val session: String?) : LoginResult
    data class Failure(val message: String) : LoginResult
}

class AuthRepository(
    private val cognito: CognitoClient,
    private val sessionStore: SessionStore,
    private val configStore: ConfigStore
) {

    private val _session = MutableStateFlow(sessionStore.load())
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val refreshMutex = Mutex()

    suspend fun login(username: String, password: String, role: UserRole): LoginResult {
        val config = configStore.snapshot()
        val cleanUsername = username.trim()
        return when (
            val result = cognito.login(
                region = config.cognitoRegion,
                clientId = config.cognitoClientId,
                username = cleanUsername,
                password = password
            )
        ) {
            is CognitoResult.Tokens -> {
                persist(result.tokens, cleanUsername, role)
                LoginResult.Success
            }
            is CognitoResult.Challenge -> {
                if (result.name == "NEW_PASSWORD_REQUIRED") {
                    LoginResult.NewPasswordRequired(result.session)
                } else {
                    LoginResult.Failure("Cognito solicita un desafío no soportado: ${result.name}")
                }
            }
            is CognitoResult.Error -> LoginResult.Failure(result.message)
        }
    }

    suspend fun completeNewPassword(
        username: String,
        newPassword: String,
        challengeSession: String?,
        role: UserRole
    ): LoginResult {
        val config = configStore.snapshot()
        val cleanUsername = username.trim()
        return when (
            val result = cognito.respondNewPassword(
                region = config.cognitoRegion,
                clientId = config.cognitoClientId,
                username = cleanUsername,
                newPassword = newPassword,
                challengeSession = challengeSession
            )
        ) {
            is CognitoResult.Tokens -> {
                persist(result.tokens, cleanUsername, role)
                LoginResult.Success
            }
            is CognitoResult.Challenge ->
                LoginResult.Failure("Cognito requiere un paso adicional no soportado por esta app.")
            is CognitoResult.Error -> LoginResult.Failure(result.message)
        }
    }

    suspend fun validIdToken(): String? {
        val current = _session.value ?: return null
        if (!current.isExpiring()) return current.idToken
        return if (refreshIfNeeded(force = true)) _session.value?.idToken else null
    }

    suspend fun refreshIfNeeded(force: Boolean = false): Boolean = refreshMutex.withLock {
        val current = _session.value ?: return@withLock false
        if (!force && !current.isExpiring()) return@withLock true

        val refreshToken = current.refreshToken ?: run {
            logout()
            return@withLock false
        }
        val config = configStore.snapshot()
        when (
            val result = cognito.refresh(
                region = config.cognitoRegion,
                clientId = config.cognitoClientId,
                refreshToken = refreshToken
            )
        ) {
            is CognitoResult.Tokens -> {
                val updated = current.copy(
                    idToken = result.tokens.idToken,
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken ?: current.refreshToken,
                    expiresAtEpochSeconds = nowEpochSeconds() + result.tokens.expiresIn - 30L
                )
                persistSession(updated)
                true
            }
            else -> {
                logout()
                false
            }
        }
    }

    fun updateRole(role: UserRole) {
        val current = _session.value ?: return
        if (current.role == role) return
        persistSession(current.copy(role = role))
    }

    fun logout() {
        sessionStore.clear()
        _session.value = null
    }

    private fun persist(tokens: CognitoTokens, username: String, role: UserRole) {
        persistSession(
            Session(
                username = username,
                idToken = tokens.idToken,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresAtEpochSeconds = nowEpochSeconds() + tokens.expiresIn - 30L,
                role = role
            )
        )
    }

    private fun persistSession(session: Session) {
        sessionStore.save(session)
        _session.value = session
    }

    private fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1000L
}
