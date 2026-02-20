package com.example.signinregister.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.R
import com.example.signinregister.data.repository.AuthRepository
import com.example.signinregister.ui.common.AuthUiState
import com.facebook.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.register(email, password, username)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.register_error)
            }
        }
    }

    fun registerWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.google_register_error)
            }
        }
    }

    fun registerWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.loginWithFacebook(token)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.facebook_register_error)
            }
        }
    }
}
