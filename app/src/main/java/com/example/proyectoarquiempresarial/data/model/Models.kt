package com.example.proyectoarquiempresarial.data.model

data class CollectionCase(
    val caseId: String,
    val accountReference: String,
    val customerReference: String,
    val currency: String,
    val overdueAmount: Double,
    val overdueDays: Int,
    val strategyId: String,
    val bucket: String,
    val action: String,
    val lateFee: Double,
    val totalDue: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class Strategy(
    val strategyId: String,
    val name: String,
    val minDays: Int,
    val maxDays: Int,
    val action: String,
    val lateFeeRate: Double,
    val channel: String,
    val currency: String,
    val validFrom: String,
    val validTo: String,
    val status: String
)

data class InitiateRequest(
    val accountReference: String,
    val customerReference: String,
    val currency: String,
    val overdueAmount: Double,
    val overdueDays: Int
)

data class RegisterStrategyRequest(
    val strategyId: String,
    val name: String,
    val minDays: Int,
    val maxDays: Int,
    val action: String,
    val lateFeeRate: Double,
    val channel: String,
    val currency: String,
    val validFrom: String,
    val validTo: String
)

data class RecentCase(
    val caseId: String,
    val accountReference: String,
    val totalDue: Double,
    val currency: String,
    val status: String,
    val savedAtMillis: Long
)
