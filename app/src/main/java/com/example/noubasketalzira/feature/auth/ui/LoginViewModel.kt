package com.example.noubasketalzira.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.IAuthDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSentTo: String? = null
)

class LoginViewModel(
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(error = "Rellena todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val success = authDataSource.loginWithEmail(email, pass)
            if (!success) {
                _uiState.update { it.copy(isLoading = false, error = "Credenciales incorrectas o error de red") }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                // App will react to SessionManager state change and navigate away
            }
        }
    }

    fun requestOtp(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Introduce tu email para recuperar la contraseña") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authDataSource.sendOtp(email)
                _uiState.update { it.copy(isLoading = false, otpSentTo = email) }
            } catch (e: Exception) {
                android.util.Log.e("NouBasketAuth", "Error sending OTP", e)
                _uiState.update { it.copy(isLoading = false, error = "Error: ${e.message ?: "Comprueba la dirección"}") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearOtpSent() {
        _uiState.update { it.copy(otpSentTo = null) }
    }
}
