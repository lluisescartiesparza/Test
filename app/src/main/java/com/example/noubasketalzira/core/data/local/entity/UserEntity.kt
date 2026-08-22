package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        role = role
    )
}
