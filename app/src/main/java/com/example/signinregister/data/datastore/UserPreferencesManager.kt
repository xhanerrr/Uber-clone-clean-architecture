package com.example.signinregister.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesManager @Inject constructor(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val FULL_NAME = stringPreferencesKey("full_name")
        val GENDER = stringPreferencesKey("gender")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[IS_LOGGED_IN] ?: false }

    val userEmailFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[USER_EMAIL] ?: "" }

    val userUidFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[USER_UID] ?: "" }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[USER_NAME] ?: "" }

    val fullNameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[FULL_NAME] ?: "" }

    val genderFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[GENDER] ?: "" }

    val phoneFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[PHONE_NUMBER] ?: "" }

    suspend fun saveUserSession(uid: String, email: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_UID] = uid
            prefs[USER_EMAIL] = email
            prefs[USER_NAME] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun updateUsername(username: String){
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = username
        }
    }

    suspend fun updateFullName(fullName: String){
        context.dataStore.edit { prefs ->
            prefs[FULL_NAME] = fullName
        }
    }

    suspend fun updateGender(gender: String){
        context.dataStore.edit { prefs ->
            prefs[GENDER] = gender
        }
    }

    suspend fun updatePhone(phone: String){
        context.dataStore.edit { prefs ->
            prefs[PHONE_NUMBER] = phone
        }
    }

    suspend fun updateEmail(email: String){
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL] = email
        }
    }
}