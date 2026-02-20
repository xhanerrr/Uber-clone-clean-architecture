package com.example.signinregister.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _logoutEvent = MutableStateFlow<Boolean>(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> = _gender

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> = _phone

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _updateStatus = MutableLiveData<String?>()
    val updateStatus: LiveData<String?> = _updateStatus

    init {
        repository.getUsernameFlow().onEach { name -> _username.value = name }.launchIn(viewModelScope)
        repository.getFullNameFlow().onEach { name -> _name.value = name }.launchIn(viewModelScope)
        repository.getGenderFlow().onEach { gender -> _gender.value = gender }.launchIn(viewModelScope)
        repository.getPhoneFlow().onEach { phone -> _phone.value = phone }.launchIn(viewModelScope)
        repository.getEmailFlow().onEach { email -> _email.value = email }.launchIn(viewModelScope)
    }


    fun updateGender(newGender: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateGender(newGender)
            handleUpdateResult(result, "Género")
        }
    }

    fun updatePhone(newPhone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updatePhone(newPhone)
            handleUpdateResult(result, "Teléfono")
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateEmail(newEmail)
            handleUpdateResult(result, "Email")
        }
    }

    fun updateUsername(newUsername: String) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {

            var statusMessage: String? = null

            try {
                repository.updateUsername(newUsername)

                statusMessage = "Nombre de usuario actualizado con éxito."

            } catch (e: Exception) {
                statusMessage = "Error al actualizar el nombre. Intenta de nuevo."

            } finally {

                _updateStatus.postValue(statusMessage)

                _isLoading.postValue(false)
            }
        }
    }

    private fun handleUpdateResult(result: Result<Unit>, fieldName: String) {
        result.onFailure { exception ->
            _updateStatus.postValue("Error al actualizar $fieldName: ${exception.message}")
        }.onSuccess {
            _updateStatus.postValue("$fieldName actualizado con éxito.")
        }
    }

    fun performLogout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.logout()
                _logoutEvent.value = true
            } catch (e: Exception) {
                _updateStatus.postValue("Error al cerrar sesión: ${e.message}")
            }
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = null
    }

}
