package com.example.proyectoarquiempresarial.core

import android.content.Context
import org.json.JSONObject

class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): Session? {
        val encoded = prefs.getString(KEY_SESSION, null) ?: return null
        val jsonText = TokenCipher.decrypt(encoded) ?: return null
        return runCatching {
            val json = JSONObject(jsonText)
            val role = runCatching {
                UserRole.valueOf(json.optString("role", UserRole.GESTOR.name))
            }.getOrDefault(UserRole.GESTOR)
            Session(
                username = json.optString("username"),
                idToken = json.optString("idToken"),
                accessToken = json.optString("accessToken"),
                refreshToken = json.optString("refreshToken").takeIf { it.isNotBlank() },
                expiresAtEpochSeconds = json.optLong("expiresAtEpochSeconds", 0L),
                role = role
            )
        }.getOrNull()?.takeIf { it.idToken.isNotBlank() }
    }

    fun save(session: Session) {
        val json = JSONObject().apply {
            put("username", session.username)
            put("idToken", session.idToken)
            put("accessToken", session.accessToken)
            put("refreshToken", session.refreshToken ?: "")
            put("expiresAtEpochSeconds", session.expiresAtEpochSeconds)
            put("role", session.role.name)
        }
        val encoded = TokenCipher.encrypt(json.toString()) ?: return
        prefs.edit().putString(KEY_SESSION, encoded).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private companion object {
        const val PREFS_NAME = "collectiq_session"
        const val KEY_SESSION = "session"
    }
}
