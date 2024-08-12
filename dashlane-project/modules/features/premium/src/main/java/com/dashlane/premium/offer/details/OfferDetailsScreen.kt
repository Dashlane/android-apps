package com.dashlane.premium.offer.details

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.premium.R
import com.dashlane.premium.offer.details.ui.ActionBottomBar
import com.dashlane.premium.offer.details.ui.BenefitsList
import com.dashlane.premium.offer.details.ui.DisclaimerContent
import com.dashlane.premium.offer.details.ui.ExtraActionButtonState
import com.dashlane.premium.offer.details.ui.PurchaseButtonState
import com.dashlane.ui.common.compose.components.LoadingScreen
import com.dashlane.util.getBaseActivity

@Composable
fun OfferDetailsScreen(viewModel: OfferDetailsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.getBaseActivity() as? AppCompatActivity ?: return
    activity.supportActionBar?.title = stringResource(id = viewModel.titleResId)

    LaunchedEffect(key1 = uiState) {
        if (uiState is OfferDetailsViewState.Error) {
            viewModel.goBack()
        }
    }
    when (val state = uiState) {
        is OfferDetailsViewState.Loading -> LoadingScreen(title = "")
        is OfferDetailsViewState.Success -> {
            val offerDetails = state.viewData.offerDetails ?: return
            SuccessScreenContent(
                warning = offerDetails.warning,
                benefits = offerDetails.benefits,
                onClickTosLink = viewModel::goToTos,
                onClickPrivacyLink = viewModel::goToPrivacy,
                monthlyButtonState = offerDetails.monthlyProduct?.let { monthly ->
                    PurchaseButtonState(
                        enabled = monthly.enabled,
                        ctaString = viewModel.getCtaString(monthly.priceInfo),
                        priceInfoString = viewModel.getMonthlyInfoString(monthly.priceInfo),
                        isPrimary = false,
                        onClick = { monthly.let { viewModel.startPurchase(activity, it) } }
                    )
                },
                yearlyButtonState = offerDetails.yearlyProduct?.let { yearly ->
                    PurchaseButtonState(
                        enabled = yearly.enabled,
                        ctaString = viewModel.getCtaString(yearly.priceInfo),
                        priceInfoString = viewModel.getYearlyInfoString(yearly.priceInfo),
                        isPrimary = true,
                        onClick = { viewModel.startPurchase(activity, yearly) }
                    )
                },
                extraActionButton = offerDetails.extraCtaString?.let {
                    ExtraActionButtonState(
                        ctaString = it,
                        onClick = {
                            viewModel.navigateToVaultPasswordSection()
                            activity.finish()
                        }
                    )
                }
            )
        }
        is OfferDetailsViewState.Error -> viewModel.goBack()
    }
}

@Composable
private fun SuccessScreenContent(
    warning: String?,
    benefits: List<String>,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
    monthlyButtonState: PurchaseButtonState?,
    yearlyButtonState: PurchaseButtonState?,
    extraActionButton: ExtraActionButtonState?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 640.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .widthIn(max = 640.dp)
        ) {
            BenefitsList(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                warning = warning,
                benefits = benefits
            )
            Spacer(modifier = Modifier.size(14.dp))
            DisclaimerContent(
                onClickTosLink = onClickTosLink,
                onClickPrivacyLink = onClickPrivacyLink,
                isVisible = monthlyButtonState != null || yearlyButtonState != null
            )
            Spacer(modifier = Modifier.size(8.dp))
            ActionBottomBar(monthlyButtonState, yearlyButtonState, extraActionButton)
        }
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun SuccessScreenContentPreview() = DashlanePreview {
    SuccessScreenContent(
        warning = stringResource(id = R.string.billing_platform_conflict_warning_app_store),
        benefits = listOf(
            stringResource(R.string.current_benefit_passwords_unlimited),
            stringResource(R.string.current_benefit_devices_sync_unlimited),
            stringResource(R.string.current_benefit_secure_notes),
            stringResource(R.string.current_benefit_sharing),
            stringResource(R.string.current_benefit_premium_plus),
        ),
        monthlyButtonState = PurchaseButtonState(
            enabled = false,
            ctaString = "$3 for 1 month",
            isPrimary = false,
            onClick = {}
        ),
        yearlyButtonState = PurchaseButtonState(
            enabled = true,
            ctaString = "$30 for 12 month",
            isPrimary = true,
            onClick = {},
            priceInfoString = "or $2.5 per month"
        ),
        extraActionButton = ExtraActionButtonState(ctaString = "Optional action button") {},
        onClickPrivacyLink = {},
        onClickTosLink = {}
    )
}