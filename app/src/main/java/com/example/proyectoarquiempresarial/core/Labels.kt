package com.example.proyectoarquiempresarial.core

object Labels {

    fun action(action: String?): String = when (action?.uppercase()) {
        "REMINDER" -> "Recordatorio"
        "CALL" -> "Llamada"
        "FIELD_VISIT" -> "Visita de campo"
        "LEGAL" -> "Gestión legal"
        "NONE" -> "Sin acción"
        else -> action?.takeIf { it.isNotBlank() } ?: "—"
    }

    fun status(status: String?): String = when (status?.uppercase()) {
        "OPEN" -> "Abierto"
        "IN_PROGRESS" -> "En gestión"
        "PROMISE" -> "Promesa de pago"
        "PAID" -> "Pagado"
        "WRITTEN_OFF" -> "Castigado"
        "ACTIVE" -> "Activa"
        "INACTIVE" -> "Inactiva"
        else -> status?.takeIf { it.isNotBlank() } ?: "—"
    }

    fun channel(channel: String?): String = when (channel?.uppercase()) {
        "SMS" -> "SMS"
        "PHONE" -> "Teléfono"
        "FIELD" -> "Campo"
        "LEGAL" -> "Legal"
        "ALL" -> "Todos"
        else -> channel?.takeIf { it.isNotBlank() } ?: "—"
    }

    fun updateStatuses(role: UserRole): List<String> = when (role) {
        UserRole.GESTOR -> listOf("IN_PROGRESS", "PROMISE", "PAID", "WRITTEN_OFF")
        UserRole.CLIENTE -> listOf("PROMISE")
    }

    val actions: List<String> = listOf("REMINDER", "CALL", "FIELD_VISIT", "LEGAL")
    val channels: List<String> = listOf("SMS", "PHONE", "FIELD", "LEGAL", "ALL")
    val currencies: List<String> = listOf("PEN", "USD")
}
