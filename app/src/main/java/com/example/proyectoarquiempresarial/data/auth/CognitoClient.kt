package com.example.proyectoarquiempresarial.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class CognitoTokens(
    val idToken: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)

sealed interface CognitoResult {
    data class Tokens(val tokens: CognitoTokens) : CognitoResult
    data class Challenge(val name: String, val session: String?) : CognitoResult
    data class Error(val message: String) : CognitoResult
}

class CognitoClient(private val client: OkHttpClient) {

    private val jsonMediaType = "application/x-amz-json-1.1".toMediaType()

    suspend fun login(
        region: String,
        clientId: String,
        username: String,
        password: String
    ): CognitoResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("AuthFlow", "USER_PASSWORD_AUTH")
            put("ClientId", clientId)
            put(
                "AuthParameters",
                JSONObject().apply {
                    put("USERNAME", username)
                    put("PASSWORD", password)
                }
            )
        }
        execute(region, "InitiateAuth", payload)
    }

    suspend fun respondNewPassword(
        region: String,
        clientId: String,
        username: String,
        newPassword: String,
        challengeSession: String?
    ): CognitoResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("ChallengeName", "NEW_PASSWORD_REQUIRED")
            put("ClientId", clientId)
            put(
                "ChallengeResponses",
                JSONObject().apply {
                    put("USERNAME", username)
                    put("NEW_PASSWORD", newPassword)
                }
            )
            if (!challengeSession.isNullOrBlank()) put("Session", challengeSession)
        }
        execute(region, "RespondToAuthChallenge", payload)
    }

    suspend fun refresh(
        region: String,
        clientId: String,
        refreshToken: String
    ): CognitoResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("AuthFlow", "REFRESH_TOKEN_AUTH")
            put("ClientId", clientId)
            put(
                "AuthParameters",
                JSONObject().apply {
                    put("REFRESH_TOKEN", refreshToken)
                }
            )
        }
        execute(region, "InitiateAuth", payload)
    }

    private fun execute(region: String, target: String, payload: JSONObject): CognitoResult {
        val request = Request.Builder()
            .url("https://cognito-idp.$region.amazonaws.com/")
            .header("Content-Type", "application/x-amz-json-1.1")
            .header("X-Amz-Target", "AWSCognitoIdentityProviderService.$target")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return CognitoResult.Error(mapError(body, response.code))
                }
                parseSuccess(body)
            }
        } catch (_: Exception) {
            CognitoResult.Error("No se pudo conectar con Amazon Cognito. Revisa tu conexión a internet.")
        }
    }

    private fun parseSuccess(body: String): CognitoResult {
        val json = runCatching { JSONObject(body) }.getOrNull()
            ?: return CognitoResult.Error("Cognito devolvió una respuesta no válida.")

        val authResult = json.optJSONObject("AuthenticationResult")
        if (authResult != null) {
            val idToken = authResult.optString("IdToken")
            val accessToken = authResult.optString("AccessToken")
            if (idToken.isBlank() || accessToken.isBlank()) {
                return CognitoResult.Error("Cognito no devolvió los tokens esperados.")
            }
            return CognitoResult.Tokens(
                CognitoTokens(
                    idToken = idToken,
                    accessToken = accessToken,
                    refreshToken = authResult.optString("RefreshToken").takeIf { it.isNotBlank() },
                    expiresIn = authResult.optLong("ExpiresIn", 3600L)
                )
            )
        }

        val challenge = json.optString("ChallengeName")
        if (challenge.isNotBlank()) {
            return CognitoResult.Challenge(
                name = challenge,
                session = json.optString("Session").takeIf { it.isNotBlank() }
            )
        }

        return CognitoResult.Error("Cognito devolvió una respuesta inesperada.")
    }

    private fun mapError(body: String, statusCode: Int): String {
        val json = runCatching { JSONObject(body) }.getOrNull()
        val type = json?.optString("__type").orEmpty()
            .substringAfterLast('#')
            .substringAfterLast(':')
        val detail = json?.optString("message").orEmpty()

        return when (type) {
            "NotAuthorizedException", "UserNotFoundException" ->
                "Usuario o contraseña incorrectos."
            "UserNotConfirmedException" ->
                "La cuenta aún no está confirmada. Contacta al administrador."
            "PasswordResetRequiredException" ->
                "Debes restablecer tu contraseña antes de iniciar sesión."
            "InvalidPasswordException" ->
                detail.ifBlank { "La contraseña no cumple la política de seguridad." }
            "TooManyRequestsException", "LimitExceededException" ->
                "Demasiados intentos. Espera un momento y vuelve a intentarlo."
            "ExpiredCodeException" ->
                "La sesión de cambio de contraseña expiró. Inicia sesión nuevamente."
            else -> detail.ifBlank { "No se pudo autenticar (HTTP $statusCode)." }
        }
    }
}
