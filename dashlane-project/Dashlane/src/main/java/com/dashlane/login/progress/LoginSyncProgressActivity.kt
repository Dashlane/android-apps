package com.dashlane.login.progress

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginIntents
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clearTask
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSyncProgressActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.safeContentPadding(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    LoginSyncProgressScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        success = ::startDashlane,
                        syncError = ::syncError,
                        cancel = { finish() }
                    )
                }
            }
        }
    }

    private fun startDashlane() {
        if (LoginIntents.shouldCloseLoginAfterSuccess(intent)) {
            finishAffinity()
            return
        }
        startActivity(LoginIntents.createHomeActivityIntent(this))
        finishAffinity()
    }

    private fun syncError() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            clearTask()
            putExtra(LoginActivity.SYNC_ERROR, true)
        }
        startActivity(intent)
        finishAffinity()
    }

    companion object {
        const val EXTRA_MONOBUCKET_UNREGISTRATION = "monobucket_unregistration"

        const val EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION = "extra_device_sync_limit_unregistration"
    }
}
