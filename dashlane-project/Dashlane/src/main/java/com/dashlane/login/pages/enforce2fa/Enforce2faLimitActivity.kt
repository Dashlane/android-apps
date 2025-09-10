package com.dashlane.login.pages.enforce2fa

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginActivity
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Enforce2faLimitActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                Enforce2faLimitScreen(
                    viewModel = hiltViewModel(),
                    goToLogin = ::goToLogin,
                )
            }
        }
    }

    private fun goToLogin() {
        val loginIntent = DashlaneIntent.newInstance(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finishAffinity()
    }
}