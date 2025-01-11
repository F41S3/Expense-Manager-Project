package com.example.csci4176project.models

/**
 * Data model used to represent a user. Invites and groups carry a list of groupIds to link the user to.
 * Expenses is a mapping of groupIds with a list of expenseIds that involve the user
 */
data class UserModel(
    val name: String? = null,
    val email: String? = null,
    val pfp: String? = null,
    val currency: String? = null,
    val invites: List<String> = listOf(),
    val groups: List<String> = listOf(),
    val expenses: Map<String, List<String>> = emptyMap()
)
