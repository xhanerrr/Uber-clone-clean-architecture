package com.example.signinregister.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.R
import com.example.signinregister.data.repository.AuthRepository
import com.example.signinregister.ui.common.AuthUiState
import com.facebook.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val isUserLoggedIn: Flow<Boolean> = repository.isUserLoggedIn()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.login(email, password)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.login_error)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.google_login_error)
            }
        }
    }

    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.loginWithFacebook(token)
            result.onSuccess {
                _authState.value = AuthUiState.Success("Bienvenido")
            }.onFailure { e ->
                _authState.value = AuthUiState.LoginError(R.string.facebook_login_error)
            }
        }
    }
}
