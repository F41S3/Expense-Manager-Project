package com.example.csci4176project.models

/**
 * Data model used to represent a group, where invites and members contain a list of userIds
 * to refer to the user DB
 */
data class GroupModel(
    val name: String? = null,
    val type: String? = null,
    val invites: List<String> = listOf(),
    val members: List<String> = listOf(),
)