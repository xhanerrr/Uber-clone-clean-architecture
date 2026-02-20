package com.example.signinregister.ui.common

import androidx.annotation.StringRes

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class LoginError(@StringRes val messageResId: Int) : AuthUiState()
}
