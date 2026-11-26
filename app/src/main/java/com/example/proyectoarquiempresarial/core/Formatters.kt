package com.example.proyectoarquiempresarial.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object Formatters {

    fun money(amount: Double?, currency: String?): String {
        val value = amount ?: 0.0
        val prefix = when (currency?.uppercase(Locale.US)) {
            "PEN" -> "S/ "
            "USD" -> "$ "
            else -> "${currency.orEmpty()} ".takeIf { it.isNotBlank() } ?: ""
        }
        return prefix + decimalFormat().format(value)
    }

    fun percent(value: Double?): String {
        val valueOrZero = value ?: 0.0
        val formatted = decimalFormat().format(valueOrZero)
        return "$formatted %"
    }

    fun days(value: Int?): String = "${value ?: 0} días"

    fun dateTime(iso: String?): String = formatIso(iso, "dd/MM/yyyy HH:mm")

    fun date(iso: String?): String = formatIso(iso, "dd/MM/yyyy")

    private fun formatIso(iso: String?, outputPattern: String): String {
        if (iso.isNullOrBlank()) return "—"
        val cleaned = iso.trim()
            .replace("Z", "")
            .substringBefore(".")
            .replace(' ', 'T')
            .take(19)
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd")
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                val parsed = parser.parse(cleaned) ?: continue
                return SimpleDateFormat(outputPattern, Locale("es", "PE")).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(parsed)
            } catch (_: Exception) {
                // Prueba el siguiente formato.
            }
        }
        return iso
    }

    private fun decimalFormat(): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        symbols.decimalSeparator = '.'
        return DecimalFormat("#,##0.00", symbols)
    }
}
