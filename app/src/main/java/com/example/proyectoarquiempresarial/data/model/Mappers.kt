package com.example.proyectoarquiempresarial.data.model

import org.json.JSONException
import org.json.JSONObject

internal fun JSONObject.toCollectionCase(): CollectionCase {
    val caseId = optString("caseId").ifBlank {
        throw JSONException("La respuesta no contiene caseId")
    }
    return CollectionCase(
        caseId = caseId,
        accountReference = optString("accountReference"),
        customerReference = optString("customerReference"),
        currency = optString("currency", "PEN"),
        overdueAmount = optDouble("overdueAmount", 0.0),
        overdueDays = optInt("overdueDays", 0),
        strategyId = optString("strategyId"),
        bucket = optString("bucket"),
        action = optString("action"),
        lateFee = optDouble("lateFee", 0.0),
        totalDue = optDouble("totalDue", 0.0),
        status = optString("status"),
        createdAt = optString("createdAt"),
        updatedAt = optString("updatedAt")
    )
}

internal fun JSONObject.toStrategy(): Strategy = Strategy(
    strategyId = optString("strategyId"),
    name = optString("name"),
    minDays = optInt("minDays", 0),
    maxDays = optInt("maxDays", 0),
    action = optString("action"),
    lateFeeRate = optDouble("lateFeeRate", 0.0),
    channel = optString("channel"),
    currency = optString("currency", "PEN"),
    validFrom = optString("validFrom"),
    validTo = optString("validTo"),
    status = optString("status")
)

internal fun CollectionCase.toRecentCase(savedAtMillis: Long = System.currentTimeMillis()): RecentCase =
    RecentCase(
        caseId = caseId,
        accountReference = accountReference,
        totalDue = totalDue,
        currency = currency,
        status = status,
        savedAtMillis = savedAtMillis
    )

internal fun RecentCase.toJson(): JSONObject = JSONObject().apply {
    put("caseId", caseId)
    put("accountReference", accountReference)
    put("totalDue", totalDue)
    put("currency", currency)
    put("status", status)
    put("savedAtMillis", savedAtMillis)
}

internal fun JSONObject.toRecentCase(): RecentCase = RecentCase(
    caseId = optString("caseId"),
    accountReference = optString("accountReference"),
    totalDue = optDouble("totalDue", 0.0),
    currency = optString("currency", "PEN"),
    status = optString("status"),
    savedAtMillis = optLong("savedAtMillis", 0L)
)
