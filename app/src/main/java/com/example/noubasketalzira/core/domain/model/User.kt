package com.example.noubasketalzira.core.domain.model

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole
)
