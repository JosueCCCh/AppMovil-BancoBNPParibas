package com.example.proyectoarquiempresarial.core

import android.content.Context
import com.example.proyectoarquiempresarial.BuildConfig

data class AppConfig(
    val apiBaseUrl: String,
    val cognitoRegion: String,
    val cognitoClientId: String,
    val userPoolId: String
)

class ConfigStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun apiBaseUrl(): String {
        val stored = prefs.getString(KEY_API_BASE_URL, null)
        return normalize(stored ?: BuildConfig.API_BASE_URL)
    }

    fun setApiBaseUrl(value: String) {
        prefs.edit().putString(KEY_API_BASE_URL, normalize(value)).apply()
    }

    fun snapshot(): AppConfig = AppConfig(
        apiBaseUrl = apiBaseUrl(),
        cognitoRegion = BuildConfig.COGNITO_REGION,
        cognitoClientId = BuildConfig.COGNITO_CLIENT_ID,
        userPoolId = BuildConfig.COGNITO_USER_POOL_ID
    )

    private fun normalize(value: String): String {
        val trimmed = value.trim().trimEnd('/')
        return if (trimmed.startsWith("https://")) trimmed else BuildConfig.API_BASE_URL
    }

    private companion object {
        const val PREFS_NAME = "collectiq_config"
        const val KEY_API_BASE_URL = "api_base_url"
    }
}
