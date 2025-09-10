package com.dashlane.disabletotp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clearTop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisableTotpEnforcedIntroActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                DisableTotpEnforcedIntroScreen(
                    onDisableTotpClick = ::goToDisableTotp,
                    onCancelClick = ::finish,
                )
            }
        }
    }

    private fun goToDisableTotp() {
        startActivity(Intent(this, DisableTotpActivity::class.java).clearTop())
        finish()
    }
}