package com.example.proyectoarquiempresarial.core

enum class UserRole(val label: String, val description: String) {
    GESTOR("Gestor", "Back-office"),
    CLIENTE("Cliente", "Autoservicio")
}

data class Session(
    val username: String,
    val idToken: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long,
    val role: UserRole
) {
    fun isExpiring(nowEpochSeconds: Long = System.currentTimeMillis() / 1000L): Boolean =
        expiresAtEpochSeconds - 120L <= nowEpochSeconds

    fun remainingMinutes(nowEpochSeconds: Long = System.currentTimeMillis() / 1000L): Long =
        ((expiresAtEpochSeconds - nowEpochSeconds).coerceAtLeast(0L)) / 60L
}
