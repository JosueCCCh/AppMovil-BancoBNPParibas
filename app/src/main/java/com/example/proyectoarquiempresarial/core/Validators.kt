package com.example.proyectoarquiempresarial.core

import java.text.SimpleDateFormat
import java.util.Locale

object Validators {

    fun initiate(
        accountReference: String,
        overdueAmount: String,
        overdueDays: String
    ): Map<String, String> {
        val errors = linkedMapOf<String, String>()

        if (accountReference.isBlank()) {
            errors["accountReference"] = "Ingresa la cuenta vencida."
        }

        val amount = overdueAmount.trim().replace(',', '.').toDoubleOrNull()
        if (amount == null) {
            errors["overdueAmount"] = "Ingresa un monto válido."
        } else if (amount <= 0.0) {
            errors["overdueAmount"] = "El monto debe ser mayor que cero."
        }

        val days = overdueDays.trim().toIntOrNull()
        if (days == null) {
            errors["overdueDays"] = "Ingresa los días de mora."
        } else if (days < 0) {
            errors["overdueDays"] = "Los días de mora no pueden ser negativos."
        }

        return errors
    }

    fun strategy(
        strategyId: String,
        name: String,
        minDays: String,
        maxDays: String,
        lateFeeRate: String,
        validFrom: String,
        validTo: String
    ): Map<String, String> {
        val errors = linkedMapOf<String, String>()

        if (strategyId.isBlank()) errors["strategyId"] = "Ingresa el identificador."
        if (name.isBlank()) errors["name"] = "Ingresa el nombre del tramo."

        val min = minDays.trim().toIntOrNull()
        val max = maxDays.trim().toIntOrNull()
        if (min == null) {
            errors["minDays"] = "Ingresa el día mínimo."
        } else if (min < 0) {
            errors["minDays"] = "No puede ser negativo."
        }
        if (max == null) {
            errors["maxDays"] = "Ingresa el día máximo."
        } else if (max < 0) {
            errors["maxDays"] = "No puede ser negativo."
        }
        if (min != null && max != null && min > max) {
            errors["maxDays"] = "Debe ser mayor o igual al mínimo."
        }

        val rate = lateFeeRate.trim().replace(',', '.').toDoubleOrNull()
        if (rate == null) {
            errors["lateFeeRate"] = "Ingresa una tasa válida."
        } else if (rate < 0.0) {
            errors["lateFeeRate"] = "La tasa no puede ser negativa."
        }

        if (!isIsoDate(validFrom)) errors["validFrom"] = "Usa el formato AAAA-MM-DD."
        if (!isIsoDate(validTo)) errors["validTo"] = "Usa el formato AAAA-MM-DD."
        if (isIsoDate(validFrom) && isIsoDate(validTo) && validFrom > validTo) {
            errors["validTo"] = "Debe ser posterior a la fecha inicial."
        }

        return errors
    }

    fun isIsoDate(value: String): Boolean = try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        formatter.parse(value.trim())
        value.trim().length == 10
    } catch (_: Exception) {
        false
    }
}
