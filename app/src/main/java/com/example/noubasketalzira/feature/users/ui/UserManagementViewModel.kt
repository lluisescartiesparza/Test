package com.example.noubasketalzira.feature.users.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.feature.users.data.repository.IUserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserManagementViewModel(
    private val userRepository: IUserRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.syncUsers()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun createUser(email: String, fullName: String, role: UserRole) {
        viewModelScope.launch {
            try {
                userRepository.createUser(email, fullName, role)
            } catch (e: Exception) {
                _error.value = "No se ha podido crear el usuario. Es posible que el correo electrónico ya esté registrado."
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(userId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar el usuario."
            }
        }
    }
}
