package com.dashlane.login.progress

import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.NavigationConstants
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginSyncProgressActivity : DashlaneActivity() {
    @Inject
    lateinit var loginIntentFactory: LoginIntentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.scaffoldBackgroundAndInsets(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    LoginSyncProgressScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        success = ::startDashlane,
                        syncError = ::syncError,
                        cancel = { finish() },
                    )
                }
            }
        }
    }

    private fun startDashlane() {
        if (intent.getBooleanExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false)) {
            finishAffinity()
            return
        }
        startActivity(loginIntentFactory.createApplicationHomeIntent(this))
        finishAffinity()
    }

    private fun syncError() {
        val intent = loginIntentFactory.createLoginActivityIntentWithError(this)
        startActivity(intent)
        finishAffinity()
    }
}