package com.example.proyectoarquiempresarial.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `formatea soles con dos decimales`() {
        assertEquals("S/ 1,040.00", Formatters.money(1040.0, "PEN"))
        assertEquals("S/ 40.00", Formatters.money(40.0, "PEN"))
    }

    @Test
    fun `formatea dolares`() {
        assertEquals("$ 1,500.50", Formatters.money(1500.5, "USD"))
    }

    @Test
    fun `formatea fecha ISO en hora local de presentacion`() {
        assertEquals("18/11/2026 15:30", Formatters.dateTime("2026-11-18T15:30:00"))
    }

    @Test
    fun `formatea fecha corta`() {
        assertEquals("01/01/2026", Formatters.date("2026-01-01"))
    }

    @Test
    fun `formatea porcentaje`() {
        assertEquals("4.00 %", Formatters.percent(4.0))
    }

    @Test
    fun `fecha vacia devuelve guion`() {
        assertEquals("—", Formatters.dateTime(null))
        assertEquals("—", Formatters.date(""))
    }
}
