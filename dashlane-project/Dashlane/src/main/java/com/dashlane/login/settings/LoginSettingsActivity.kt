package com.dashlane.login.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginIntents
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSettingsActivity : DashlaneActivity() {

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.safeContentPadding(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    LoginSettingsScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        success = ::goToLoginSyncProgress
                    )
                }
            }
        }
    }

    private fun goToLoginSyncProgress() {
        val intent = LoginIntents.createProgressActivityIntent(this)
        startActivity(intent)
        finishAffinity()
    }
}