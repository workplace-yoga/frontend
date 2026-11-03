package com.adaptiveTrust.mobile.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.models.IntegrationUpdateRequest
import com.adaptiveTrust.mobile.viewmodel.IntegrationsViewModel
import com.adaptiveTrust.mobile.viewmodel.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntegrationsFragment : Fragment() {

    private val vm: IntegrationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_integrations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etWazuhUrl = view.findViewById<EditText>(R.id.et_wazuh_url)
        val etWazuhUser = view.findViewById<EditText>(R.id.et_wazuh_user)
        val etWazuhPass = view.findViewById<EditText>(R.id.et_wazuh_pass)
        val etKeycloakUrl = view.findViewById<EditText>(R.id.et_keycloak_url)
        val etKeycloakRealm = view.findViewById<EditText>(R.id.et_keycloak_realm)
        val etKeycloakClientId = view.findViewById<EditText>(R.id.et_keycloak_client_id)
        val cbSimulate = view.findViewById<CheckBox>(R.id.cb_simulate_data)
        
        val btnSave = view.findViewById<Button>(R.id.btn_save_integrations)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_integrations)
        val tvResult = view.findViewById<TextView>(R.id.tv_integrations_result)

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> progressBar.visibility = View.VISIBLE
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = state.message
                    tvResult.setTextColor(Color.parseColor("#FF1744"))
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    val data = state.data
                    etWazuhUrl.setText(data.wazuhApiUrl ?: "")
                    etWazuhUser.setText(data.wazuhApiUser ?: "")
                    etWazuhPass.setText("") // don't show existing password
                    etKeycloakUrl.setText(data.keycloakUrl ?: "")
                    etKeycloakRealm.setText(data.keycloakRealm ?: "")
                    etKeycloakClientId.setText(data.keycloakClientId ?: "")
                    cbSimulate.isChecked = data.simulateData
                }
            }
        }

        vm.updateResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvResult.visibility = View.GONE
                    btnSave.isEnabled = false
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = state.message
                    tvResult.setTextColor(Color.parseColor("#FF1744"))
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = "Integrations saved successfully."
                    tvResult.setTextColor(Color.parseColor("#00E676"))
                    etWazuhPass.setText("")
                }
            }
        }

        btnSave.setOnClickListener {
            // Note: Since this doesn't explicitly trigger trust score recalculation directly (it just updates config), 
            // we omit Biometric prompt for brevity, but in real app it might be required.
            val request = IntegrationUpdateRequest(
                wazuhApiUrl = etWazuhUrl.text.toString().takeIf { it.isNotBlank() },
                wazuhApiUser = etWazuhUser.text.toString().takeIf { it.isNotBlank() },
                wazuhApiPass = etWazuhPass.text.toString().takeIf { it.isNotBlank() },
                keycloakUrl = etKeycloakUrl.text.toString().takeIf { it.isNotBlank() },
                keycloakRealm = etKeycloakRealm.text.toString().takeIf { it.isNotBlank() },
                keycloakClientId = etKeycloakClientId.text.toString().takeIf { it.isNotBlank() },
                simulateData = cbSimulate.isChecked
            )
            vm.update(request)
        }
    }
}
