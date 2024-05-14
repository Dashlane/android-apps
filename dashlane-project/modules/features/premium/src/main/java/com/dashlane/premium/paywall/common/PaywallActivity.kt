package com.dashlane.premium.paywall.common

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaywallActivity : DashlaneActivity() {

    private val viewModel: PaywallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                PaywallScreen(
                    viewModel = viewModel,
                    navigateUp = {
                        viewModel.onClickClose()
                        finish()
                    },
                    navigateToOffer = {
                        viewModel.onClickUpgrade()
                        finish()
                    }
                )
            }
        }
        if (savedInstanceState == null) {
            this.setCurrentPageView(viewModel.paywallIntroState.page)
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            viewModel.onLeaving()
        }
        super.onDestroy()
    }

    companion object {
        const val PAYWALL_INTRO_TYPE_KEY = "paywallIntroType"
    }
}
