package com.adaptiveTrust.mobile.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.api.TokenStore
import com.adaptiveTrust.mobile.ui.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var tokenStore: TokenStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etAdminUser = view.findViewById<TextInputEditText>(R.id.et_admin_user)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)

        // Show the currently logged in admin user
        val currentUser = tokenStore.username ?: "Unknown Admin"
        etAdminUser.setText(currentUser)

        btnLogout.setOnClickListener {
            // Terminate session
            tokenStore.clearToken()
            Toast.makeText(requireContext(), "Session Terminated", Toast.LENGTH_SHORT).show()
            
            // Redirect to Login
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
