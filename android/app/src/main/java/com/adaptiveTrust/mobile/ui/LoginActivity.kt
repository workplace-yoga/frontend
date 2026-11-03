package com.adaptiveTrust.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.local.TokenStore
import com.adaptiveTrust.mobile.viewmodel.LoginViewModel
import com.adaptiveTrust.mobile.viewmodel.UiState
import com.adaptiveTrust.mobile.worker.TrustSyncWorker
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val vm: LoginViewModel by viewModels()
    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip login if already authenticated
        if (tokenStore.isLoggedInBlocking()) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.et_username)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val btnLogin   = findViewById<MaterialButton>(R.id.btn_login)
        val progress   = findViewById<CircularProgressIndicator>(R.id.progress_login)
        val etOtp      = findViewById<TextInputEditText>(R.id.et_otp)
        val tilUsername= findViewById<View>(R.id.til_username)
        val tilPassword= findViewById<View>(R.id.til_password)
        val tilOtp     = findViewById<View>(R.id.til_otp)
        val btnVerify  = findViewById<MaterialButton>(R.id.btn_verify_otp)
        val tvGoToRegister = findViewById<android.widget.TextView>(R.id.tv_go_to_register)

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            }

        btnLogin.setOnClickListener {
            val user = etUsername.text?.toString()?.trim() ?: ""
            val pass = etPassword.text?.toString() ?: ""
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.requestOtp(user, pass)
        }

        btnVerify.setOnClickListener {
            val user = etUsername.text?.toString()?.trim() ?: ""
            val otp = etOtp.text?.toString()?.trim() ?: ""
            if (otp.length != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.verifyOtp(user, otp)
        }

        vm.otpState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progress.show()
                    btnLogin.isEnabled = false
                }
                is UiState.Success -> {
                    progress.hide()
                    Toast.makeText(this, state.data.message, Toast.LENGTH_SHORT).show()
                    
                    val tvInstruction = findViewById<android.widget.TextView>(R.id.tv_otp_instruction)
                    
                    // Hide step 1, show step 2
                    tilUsername.visibility = View.GONE
                    tilPassword.visibility = View.GONE
                    btnLogin.visibility = View.GONE
                    
                    tvInstruction.visibility = View.VISIBLE
                    tilOtp.visibility = View.VISIBLE
                    btnVerify.visibility = View.VISIBLE
                }
                is UiState.Error -> {
                    progress.hide()
                    btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        vm.loginState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progress.show()
                    btnVerify.isEnabled = false
                }
                is UiState.Success -> {
                    progress.hide()
                    btnVerify.isEnabled = true
                    // Start background sync worker and one-shot refresh
                    TrustSyncWorker.enqueue(applicationContext)
                    TrustSyncWorker.runOnce(applicationContext)
                    goToMain()
                }
                is UiState.Error -> {
                    progress.hide()
                    btnVerify.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
