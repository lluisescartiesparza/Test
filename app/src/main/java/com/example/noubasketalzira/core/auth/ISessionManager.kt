package com.example.noubasketalzira.core.auth

import com.example.noubasketalzira.core.data.local.entity.UserEntity
import com.example.noubasketalzira.core.data.local.entity.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface ISessionManager {
    val currentUser: StateFlow<UserEntity?>
}

class MockSessionManager : ISessionManager {
    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity(
            id = UUID.randomUUID().toString(),
            email = "director@test.com",
            fullName = "Director Mock",
            role = UserRole.DIRECTOR_DEPORTIVO
        )
    )
    override val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
}
