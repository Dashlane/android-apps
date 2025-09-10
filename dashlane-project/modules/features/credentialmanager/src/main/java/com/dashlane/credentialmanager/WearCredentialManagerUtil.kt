package com.dashlane.credentialmanager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.credentials.CredentialManager
import android.os.Build
import android.provider.Settings
import com.dashlane.credentialmanager.ActivationState.Disabled
import com.dashlane.credentialmanager.ActivationState.Enabled
import com.dashlane.credentialmanager.ActivationState.Unknown
import com.dashlane.credentialmanager.ActivationState.Unsupported
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WearCredentialManagerUtil @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val isAvailable: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

    fun isCredentialProviderDisabled() = when (getActivationState()) {
        Disabled, Unknown -> true
        Unsupported, Enabled -> false
    }

    fun enableCredentialProvider() {
        if (!isAvailable) return
        val intent = Intent(Settings.ACTION_CREDENTIAL_PROVIDER).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent, null)
    }

    private fun getActivationState(): ActivationState {
        if (!isAvailable) {
            return Unsupported
        }

        val credentialManager = context.getSystemService(Context.CREDENTIAL_SERVICE) as? CredentialManager
            ?: return Unknown

        return try {
            
            val isDashlaneEnabled = credentialManager.isEnabledCredentialProviderService(
                ComponentName(
                    context,
                    DashlaneCredentialProviderService::class.java,
                ),
            )
            if (isDashlaneEnabled) {
                Enabled
            } else {
                Disabled
            }
        } catch (e: IllegalArgumentException) {
            handleException(e)
        } catch (e: NullPointerException) {
            handleException(e)
        }
    }

    private fun handleException(e: Exception): ActivationState {
            tag = "CredentialManager",
            message = "Could not check the Credential manager state",
            throwable = e,
        )
        return Unknown
    }
}

sealed class ActivationState {
    data object Enabled : ActivationState()

    data object Disabled : ActivationState()

    data object Unknown : ActivationState()

    data object Unsupported : ActivationState()
}