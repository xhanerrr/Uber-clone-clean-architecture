    package com.example.signinregister.ui.login
    
    import android.Manifest
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.os.Build
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.util.Patterns
    import android.view.View
    import android.view.inputmethod.EditorInfo
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.viewModels
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.core.view.isVisible
    import androidx.lifecycle.lifecycleScope
    import com.example.signinregister.R
    import com.example.signinregister.databinding.ActivityLoginBinding
    import com.example.signinregister.ui.common.AuthUiState
    import com.example.signinregister.ui.main.MainActivity
    import com.example.signinregister.ui.register.RegisterActivity
    import com.facebook.*
    import com.facebook.login.LoginResult
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInClient
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.android.gms.common.api.ApiException
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.launch
    
    
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    
    @AndroidEntryPoint
    class LoginActivity : AppCompatActivity() {
    
        private lateinit var binding: ActivityLoginBinding
        private val viewModel: LoginViewModel by viewModels()
    
        private lateinit var googleSignInClient: GoogleSignInClient
        private val GOOGLE_SIGN_IN = 100
    
        private lateinit var callbackManager: CallbackManager
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
    
            setupGoogleSignIn()
            setupFacebookSignIn()
            setupListeners()
            observeAuthState()
            checkIfUserLoggedIn()
            requestNotificationPermission()
    
            updateLoginButtonState()
        }
    
        private fun checkIfUserLoggedIn() {
            lifecycleScope.launch {
                val loggedIn = viewModel.isUserLoggedIn.first()
                if (loggedIn) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
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

            binding.facebookLoginBtn.setOnClickListener {
                binding.loginBtnDone.isEnabled = false
                binding.loginBtnDone.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            }

            binding.facebookLoginBtn.setPermissions("email", "public_profile")

            binding.facebookLoginBtn.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult) {
                        viewModel.loginWithFacebook(result.accessToken)
                    }

                    override fun onCancel() {
                        updateLoginButtonState()
                        Toast.makeText(this@LoginActivity, "Inicio cancelado", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        updateLoginButtonState()
                        Toast.makeText(this@LoginActivity, "Error en Facebook login: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    
        private fun setupListeners() {
            binding.txtViewRegisterIntent.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
    
            val performLogin = {
                val email = binding.loginEmailEt.text.toString().trim()
                val password = binding.loginPasswordEt.text.toString().trim()
    
                if (isEmailValid(email) && isPasswordValid(password)) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(this, "Completa o corrige todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
    
            binding.loginBtnDone.setOnClickListener {
                performLogin()
            }
    
            binding.signInButton.setOnClickListener {

                binding.loginBtnDone.isEnabled = false
                binding.loginBtnDone.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
    
    
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    

            }
    
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.hashCode() == binding.loginEmailEt.text.hashCode()) {
                        validateEmailInput(s.toString())
                    }
                    updateLoginButtonState()
                }
                override fun afterTextChanged(s: Editable?) {}
            }
    
            binding.loginEmailEt.addTextChangedListener(textWatcher)
            binding.loginPasswordEt.addTextChangedListener(textWatcher)
    
            binding.loginPasswordEt.setOnEditorActionListener(
                TextView.OnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        performLogin()
                        return@OnEditorActionListener true
                    }
                    false
                })
        }
    
        private fun observeAuthState() {
            lifecycleScope.launchWhenStarted {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {}
                        is AuthUiState.Loading -> {
                            showLoading()
                        }
                        is AuthUiState.Success -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        is AuthUiState.LoginError -> {
                            hideLoading()
                            Toast.makeText(this@LoginActivity, state.messageResId, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    
        private fun isEmailValid(email: String): Boolean {
            return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    
        private fun isPasswordValid(password: String): Boolean {
            return password.isNotEmpty()
        }
    
        private fun validateEmailInput(email: String) {
            if (email.isEmpty()) {
                binding.textInputLayout.error = null
            } else if (!isEmailValid(email)) {
                binding.textInputLayout.error = "Ingrese un correo valido"
            } else {
                binding.textInputLayout.error = null
                binding.textInputLayout.isErrorEnabled = false
            }
        }
    
        private fun updateLoginButtonState() {
            val email = binding.loginEmailEt.text.toString().trim()
            val password = binding.loginPasswordEt.text.toString().trim()
    
            val isReady = isEmailValid(email) && isPasswordValid(password)
    
            binding.loginBtnDone.isEnabled = isReady
    
            if (isReady) {
                binding.loginBtnDone.backgroundTintList = ContextCompat.getColorStateList(this, R.color.black)
            } else {
                binding.loginBtnDone.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }
    
        private fun requestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
            if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Las notificaciones están desactivadas.", Toast.LENGTH_SHORT).show()
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
                    viewModel.loginWithGoogle(idToken)
                } catch (e: ApiException) {
                    updateLoginButtonState()
                    Toast.makeText(this, "No inicio sesion correctamente", Toast.LENGTH_SHORT).show()
                }
            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
    
        private fun setContentVisibility(visibility: Int) {
            val contentViews = listOf(
                binding.titleTextView,
                binding.textInputLayout,
                binding.textInputLayout2,
                binding.loginBtnDone,
                binding.orLoginWithTextView,
                binding.signInButton,
                binding.facebookLoginBtn,
                binding.noAccountTextView,
                binding.txtViewRegisterIntent
            )
    
            contentViews.forEach { view ->
                view.visibility = visibility
    
                if (visibility == View.INVISIBLE) {
                    view.isEnabled = false
                } else if (visibility == View.VISIBLE) {
                    view.isEnabled = true
                }
            }
        }
    
        private fun showLoading() {
            setContentVisibility(View.INVISIBLE)
            binding.loadingLoginSpinner.visibility = View.VISIBLE
        }
    
        private fun hideLoading() {
            setContentVisibility(View.VISIBLE)
            binding.loadingLoginSpinner.visibility = View.GONE
        }
    
    
    }