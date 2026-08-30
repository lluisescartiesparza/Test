package com.example.noubasketalzira.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.IAuthDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OtpState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class OtpViewModel(
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpState())
    val uiState: StateFlow<OtpState> = _uiState.asStateFlow()

    fun verifyAndSetPassword(email: String, otp: String, newPass: String) {
        if (otp.isBlank() || newPass.isBlank()) {
            _uiState.update { it.copy(error = "Rellena todos los campos") }
            return
        }
        if (newPass.length < 6) {
            _uiState.update { it.copy(error = "La contrasea debe tener al menos 6 caracteres") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val success = authDataSource.verifyOtpAndSetPassword(email, otp, newPass)
            if (success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Cdigo incorrecto o expirado") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
