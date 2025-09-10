package com.dashlane.login.lock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clearTop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingApplicationLockActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                OnboardingApplicationLockScreen(
                    viewModel = hiltViewModel(),
                    goToNextStep = ::goToNextStep,
                )
            }
        }
    }

    private fun goToNextStep(nextIntent: Intent) {
        startActivity(nextIntent.clearTop())
        finish()
    }

    companion object {
        internal const val EXTRA_NEXT_INTENT = "next_intent"

        fun newIntent(context: Context, nextIntent: Intent): Intent =
            Intent(context, OnboardingApplicationLockActivity::class.java)
                .putExtra(EXTRA_NEXT_INTENT, nextIntent)
    }
}