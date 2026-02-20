package com.example.signinregister.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.signinregister.data.repository.AuthRepository
import com.example.signinregister.ui.login.LoginActivity
import com.example.signinregister.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private var isSessionChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !isSessionChecked
        }

        lifecycleScope.launch {
            val isLoggedIn = authRepository.isUserLoggedIn().first()

            isSessionChecked = true

            checkUserSessionAndRedirect(isLoggedIn)
        }
    }

    private fun checkUserSessionAndRedirect(isLoggedIn: Boolean) {

        val nextActivity = if (isLoggedIn) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, nextActivity).apply {
            if (isLoggedIn) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        })

        finish()
    }
}