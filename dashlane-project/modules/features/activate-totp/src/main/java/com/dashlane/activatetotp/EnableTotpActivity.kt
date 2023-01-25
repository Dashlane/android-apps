package com.dashlane.activatetotp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EnableTotpActivity : DashlaneActivity() {
    @Inject
    internal lateinit var logger: ActivateTotpLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_totp)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logger.logActivationCancel()
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }
}