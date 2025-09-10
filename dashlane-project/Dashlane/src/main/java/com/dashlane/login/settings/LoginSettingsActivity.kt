package com.dashlane.login.settings

import android.annotation.SuppressLint
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
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginSettingsActivity : DashlaneActivity() {
    @Inject
    lateinit var loginIntentFactory: LoginIntentFactory

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.scaffoldBackgroundAndInsets(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    LoginSettingsScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        success = ::goToLoginSyncProgress,
                    )
                }
            }
        }
    }

    private fun goToLoginSyncProgress() {
        val intent = loginIntentFactory.createLoginSyncProgressIntent(this)
        startActivity(intent)
        finishAffinity()
    }
}