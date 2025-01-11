package com.example.csci4176project.models

/**
 * Data model used to represent an expense. participants maps user ids to the amount they owe
 */
data class ExpenseModel(
    val expenseName : String? = null,
    val description: String? = null,
    val type: String? = null,
    val amount: Double? = 0.0,
    val payerId: String? = null,
    val expensePaidOff: Boolean = false,
    val participants : Map<String, Double> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)