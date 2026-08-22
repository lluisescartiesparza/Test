package com.example.noubasketalzira.core.auth

import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface ISessionManager {
    val currentUser: StateFlow<User?>
}

class MockSessionManager : ISessionManager {
    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = UUID.randomUUID().toString(),
            email = "director@test.com",
            fullName = "Director Mock",
            role = UserRole.DIRECTOR_DEPORTIVO
        )
    )
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
}
