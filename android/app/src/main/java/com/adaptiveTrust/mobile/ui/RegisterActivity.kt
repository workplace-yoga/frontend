package com.adaptiveTrust.mobile.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.viewmodel.LoginViewModel
import com.adaptiveTrust.mobile.viewmodel.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etCompany = findViewById<TextInputEditText>(R.id.et_company)
        val etAdminUser = findViewById<TextInputEditText>(R.id.et_admin_user)
        val etAdminEmail = findViewById<TextInputEditText>(R.id.et_admin_email)
        val etAdminPass = findViewById<TextInputEditText>(R.id.et_admin_pass)
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register)
        val progress = findViewById<CircularProgressIndicator>(R.id.progress_register)
        val tvBackToLogin = findViewById<android.widget.TextView>(R.id.tv_back_to_login)

        tvBackToLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val company = etCompany.text?.toString()?.trim() ?: ""
            val user = etAdminUser.text?.toString()?.trim() ?: ""
            val email = etAdminEmail.text?.toString()?.trim() ?: ""
            val pass = etAdminPass.text?.toString() ?: ""

            if (company.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.registerCompany(company, user, email, pass)
        }

        vm.registerState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progress.show()
                    btnRegister.isEnabled = false
                }
                is UiState.Success -> {
                    progress.hide()
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "Company Registered! Please log in.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is UiState.Error -> {
                    progress.hide()
                    btnRegister.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
