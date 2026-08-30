package com.example.noubasketalzira.feature.users.data.repository

import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun createUser(email: String, fullName: String, role: UserRole)
    suspend fun deleteUser(userId: String)
    suspend fun syncUsers()
}
