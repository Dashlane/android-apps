package com.dashlane.premium.paywall.common

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
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
                Scaffold { contentPadding ->
                    PaywallScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        navigateUp = {
                            viewModel.onNavigateUp()
                            finish()
                        },
                        navigateToOffer = {
                            viewModel.onClickUpgrade()
                            finish()
                        },
                        onCloseClick = {
                            viewModel.onClickClose()
                            finish()
                        },
                        onCancelClick = {
                            viewModel.onClickCancel()
                            finish()
                        },
                        onAllOffersClick = {
                            viewModel.onClickAllOffers()
                        }
                    )
                }
            }
        }
        if (savedInstanceState == null) {
            viewModel.paywallIntroState.page?.let { this.setCurrentPageView(it) }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            viewModel.onLeaving()
        }
        super.onDestroy()
    }

    companion object {
        const val PAYWALL_INTRO_TYPE_ARG = "paywallIntroType"
    }
}
