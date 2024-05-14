package com.dashlane.credentialmanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.provider.PendingIntentHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.credentialmanager.model.PasswordLimitReachedException
import com.dashlane.limitations.PasswordLimitBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@RequiresApi(34)
@AndroidEntryPoint
class CredentialManagerActivity : AppCompatActivity() {
    val viewModel by viewModels<CredentialManagerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.start(intent)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { state ->
                    updateViewState(state)
                }
            }
        }
    }

    private fun updateViewState(state: CredentialManagerState) {
        when (state) {
            is CredentialManagerState.Unlocking -> {
                viewModel.unlockDashlaneIfNeeded()
            }
            is CredentialManagerState.AuthSuccess, is CredentialManagerState.CreateSuccess -> {
                
                val result = Intent()
                if (state is CredentialManagerState.AuthSuccess) {
                    PendingIntentHandler.setGetCredentialResponse(result, state.response)
                } else if (state is CredentialManagerState.CreateSuccess) {
                    PendingIntentHandler.setCreateCredentialResponse(result, state.response)
                }

                
                setResult(RESULT_OK, result)
                finish()
            }
            is CredentialManagerState.Error -> {
                
                setResult(RESULT_CANCELED)
                if (state.e is PasswordLimitReachedException) {
                    PasswordLimitBottomSheet().show(supportFragmentManager, null)
                } else {
                    finish()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isChangingConfigurations) {
            viewModel.setConfigurationChanging()
        }
    }
}