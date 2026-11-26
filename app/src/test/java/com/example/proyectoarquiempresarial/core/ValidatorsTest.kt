package com.example.proyectoarquiempresarial.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `initiate con datos validos no devuelve errores`() {
        val errors = Validators.initiate("ACC-00017", "1000", "45")
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `initiate rechaza monto ausente o negativo`() {
        assertTrue(Validators.initiate("ACC-00017", "", "45").containsKey("overdueAmount"))
        assertTrue(Validators.initiate("ACC-00017", "-5", "45").containsKey("overdueAmount"))
        assertTrue(Validators.initiate("ACC-00017", "abc", "45").containsKey("overdueAmount"))
    }

    @Test
    fun `initiate rechaza cuenta vacia y dias negativos`() {
        val errors = Validators.initiate("", "1000", "-1")
        assertTrue(errors.containsKey("accountReference"))
        assertTrue(errors.containsKey("overdueDays"))
    }

    @Test
    fun `strategy valida un tramo correcto`() {
        val errors = Validators.strategy(
            strategyId = "STR-005",
            name = "Tramo 121+ dias",
            minDays = "121",
            maxDays = "9999",
            lateFeeRate = "10",
            validFrom = "2026-01-01",
            validTo = "2026-12-31"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `strategy rechaza rango invertido y fechas invalidas`() {
        val errors = Validators.strategy(
            strategyId = "STR-005",
            name = "Tramo",
            minDays = "200",
            maxDays = "100",
            lateFeeRate = "-1",
            validFrom = "2026-13-01",
            validTo = "2026-12-31"
        )
        assertTrue(errors.containsKey("maxDays"))
        assertTrue(errors.containsKey("lateFeeRate"))
        assertTrue(errors.containsKey("validFrom"))
    }

    @Test
    fun `isIsoDate acepta solo el formato esperado`() {
        assertTrue(Validators.isIsoDate("2026-11-18"))
        assertFalse(Validators.isIsoDate("18/11/2026"))
        assertFalse(Validators.isIsoDate("2026-11-18T10:00:00"))
        assertFalse(Validators.isIsoDate("2026-13-01"))
    }

    @Test
    fun `updateStatuses respeta el rol`() {
        assertEquals(listOf("PROMISE"), Labels.updateStatuses(UserRole.CLIENTE))
        assertTrue(Labels.updateStatuses(UserRole.GESTOR).contains("PAID"))
    }
}
