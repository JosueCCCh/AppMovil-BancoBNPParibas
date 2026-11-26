package com.example.proyectoarquiempresarial.data.model

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MappersTest {

    @Test
    fun `mapea la respuesta real de Initiate`() {
        val json = JSONObject(
            """
            {
              "caseId": "CASE-20261118-C8DEB0",
              "accountReference": "ACC-00017",
              "customerReference": "PARTY-000123",
              "currency": "PEN",
              "overdueAmount": 1000,
              "overdueDays": 45,
              "strategyId": "STR-002",
              "bucket": "Tramo 31-60 dias",
              "action": "CALL",
              "lateFee": 40,
              "totalDue": 1040,
              "status": "OPEN",
              "createdAt": "2026-11-18T10:00:00+00:00",
              "updatedAt": "2026-11-18T10:00:00+00:00"
            }
            """.trimIndent()
        )

        val collectionCase = json.toCollectionCase()

        assertEquals("CASE-20261118-C8DEB0", collectionCase.caseId)
        assertEquals("ACC-00017", collectionCase.accountReference)
        assertEquals(1000.0, collectionCase.overdueAmount, 0.001)
        assertEquals(45, collectionCase.overdueDays)
        assertEquals("STR-002", collectionCase.strategyId)
        assertEquals("CALL", collectionCase.action)
        assertEquals(40.0, collectionCase.lateFee, 0.001)
        assertEquals(1040.0, collectionCase.totalDue, 0.001)
        assertEquals("OPEN", collectionCase.status)
    }

    @Test
    fun `mapea una estrategia activa`() {
        val json = JSONObject(
            """
            {
              "strategyId": "STR-001",
              "name": "Tramo 1-30 dias",
              "minDays": 1,
              "maxDays": 30,
              "action": "REMINDER",
              "lateFeeRate": 2,
              "channel": "SMS",
              "currency": "PEN",
              "validFrom": "2026-01-01",
              "validTo": "2026-12-31",
              "status": "ACTIVE"
            }
            """.trimIndent()
        )

        val strategy = json.toStrategy()

        assertEquals("STR-001", strategy.strategyId)
        assertEquals(1, strategy.minDays)
        assertEquals(30, strategy.maxDays)
        assertEquals("REMINDER", strategy.action)
        assertEquals(2.0, strategy.lateFeeRate, 0.001)
        assertEquals("ACTIVE", strategy.status)
    }

    @Test
    fun `rechaza una respuesta sin caseId`() {
        val json = JSONObject("""{"accountReference":"ACC-00017"}""")
        assertThrows(Exception::class.java) { json.toCollectionCase() }
    }

    @Test
    fun `mapea un caso reciente y lo serializa`() {
        val original = RecentCase(
            caseId = "CASE-1",
            accountReference = "ACC-1",
            totalDue = 1040.0,
            currency = "PEN",
            status = "OPEN",
            savedAtMillis = 1234L
        )

        val restored = original.toJson().toRecentCase()

        assertEquals(original, restored)
    }
}
