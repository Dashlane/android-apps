package com.dashlane.vpn.thirdparty

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.vpn.thirdparty.ui.DisabledVpnB2bScreen

class VpnB2bIntroActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                DisabledVpnB2bScreen(
                    onCloseClick = ::finish,
                )
            }
        }
    }
}