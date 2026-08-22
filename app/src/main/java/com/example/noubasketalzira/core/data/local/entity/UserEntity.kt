package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole {
    GERENCIA, DIRECTOR_DEPORTIVO, ENTRENADOR, JUGADOR
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)
