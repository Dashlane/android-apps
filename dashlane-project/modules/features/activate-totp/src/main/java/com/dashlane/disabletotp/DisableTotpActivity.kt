package com.dashlane.disabletotp

import android.os.Bundle
import com.dashlane.activatetotp.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisableTotpActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disable_totp)
    }
}
