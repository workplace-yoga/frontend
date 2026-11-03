package com.adaptiveTrust.mobile.ui

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.UUID

/**
 * BiometricHelper – wraps AndroidX BiometricPrompt.
 *
 * HIGH-RISK ACTIONS (nudge / sensitivity change) MUST call authenticate()
 * before the API call is dispatched. A challenge token is generated on
 * success and sent to the server for verification.
 */
object BiometricHelper {

    fun isAvailable(activity: FragmentActivity): Boolean {
        val bm = BiometricManager.from(activity)
        return bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Launches biometric prompt. Calls [onSuccess] with a challenge token
     * (prefixed "BIO-") that the ViewModel passes to the API.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Identity Verification",
        subtitle: String = "Confirm your identity to proceed",
        description: String = "This action requires biometric authentication.",
        onSuccess: (biometricToken: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Generate a short-lived challenge token
                val token = "BIO-${UUID.randomUUID().toString().replace("-", "").take(24)}"
                onSuccess(token)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure("Auth error: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onFailure here; system handles retry UI automatically
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                else
                    BIOMETRIC_STRONG
            )
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setNegativeButtonText("Cancel")
                }
            }
            .build()

        prompt.authenticate(promptInfo)
    }
}
