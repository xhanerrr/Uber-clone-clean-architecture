package com.example.signinregister.ui.register

import android.app.PendingIntent
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.signinregister.R
import com.example.signinregister.databinding.ActivityRegisterBinding
import com.example.signinregister.ui.common.AuthUiState
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN = 101

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupFacebookSignIn()
        setupListeners()
        observeAuthState()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupFacebookSignIn() {
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        binding.facebookRegisterBtn.setPermissions("email", "public_profile")

        binding.facebookRegisterBtn.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.registerWithFacebook(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@RegisterActivity, "Registro cancelado", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@RegisterActivity, "Error en Facebook register: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupListeners() {

            binding.txtViewLoginIntent.setOnClickListener {
                finish()

            }

        binding.registerBtnDone.setOnClickListener {
            val email = binding.registerEmailEt.text.toString().trim()
            val password = binding.registerPasswordEt.text.toString().trim()
            val username = binding.registerUserEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(email, password, username)
        }

        binding.registerGoogleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launchWhenStarted {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {}
                    is AuthUiState.Loading -> {
                    }
                    is AuthUiState.Success -> {
                        showRegistrationNotification()
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, com.example.signinregister.ui.main.MainActivity::class.java))
                        finish()
                    }
                    is AuthUiState.LoginError -> {
                        Toast.makeText(this@RegisterActivity, state.messageResId, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken!!
                viewModel.registerWithGoogle(idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showRegistrationNotification() {
        val channelId = "registro_channel"
        val channelName = "Registro de Usuario"
        val channelDescription = "Notificaciones relacionadas con el registro de usuario"

        if (SDK_INT >= TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 123)
                return
            }
        }

        val channel = android.app.NotificationChannel(
            channelId,
            channelName,
            android.app.NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = channelDescription
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val intent = Intent(this, com.example.signinregister.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("¡Registro Exitoso!")
            .setContentText("Tu cuenta fue creada correctamente.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }
}
