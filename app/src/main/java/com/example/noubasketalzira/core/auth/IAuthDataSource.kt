package com.example.noubasketalzira.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IAuthDataSource {
    suspend fun loginWithEmail(email: String, password: String): Boolean
    suspend fun logout()
    suspend fun sendOtp(email: String)
    suspend fun verifyOtpAndSetPassword(email: String, otp: String, newPassword: String): Boolean
}

class SupabaseAuthDataSource(
    private val supabase: SupabaseClient
) : IAuthDataSource {
    
    override suspend fun loginWithEmail(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override suspend fun sendOtp(email: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.signInWith(OTP) {
                this.email = email
            }
        }
    }

    override suspend fun verifyOtpAndSetPassword(email: String, otp: String, newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verify OTP
                supabase.auth.verifyEmailOtp(
                    type = io.github.jan.supabase.auth.OtpType.Email.MAGIC_LINK,
                    email = email,
                    token = otp
                )
                // 2. Update password since we are now authenticated
                supabase.auth.updateUser {
                    password = newPassword
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
