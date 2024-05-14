@file:OptIn(ExperimentalLayoutApi::class)

package com.dashlane.premium.offer.details

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Icon
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.OfferDetails
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.widgets.compose.CircularProgressIndicator
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
        is OfferDetailsViewState.Loading -> LoadingScreenContent()
        is OfferDetailsViewState.Success -> SuccessScreenContent(
            state = state,
            ctaString = { viewModel.getCtaString(it) },
            monthlyPriceInfoString = { viewModel.getMonthlyInfoString(it) },
            yearlyPriceInfoString = { viewModel.getYearlyInfoString(it) },
            onClickTosLink = viewModel::goToTos,
            onClickPrivacyLink = viewModel::goToPrivacy,
            onClickPurchase = { viewModel.startPurchase(activity, it) }
        )
        is OfferDetailsViewState.Error -> viewModel.goBack()
    }
}

@Composable
private fun LoadingScreenContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessScreenContent(
    state: OfferDetailsViewState.Success,
    ctaString: (OfferDetails.PriceInfo?) -> String?,
    monthlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    yearlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    if (portrait) {
        SuccessScreenContentPortrait(
            state = state,
            ctaString = ctaString,
            monthlyPriceInfoString = monthlyPriceInfoString,
            yearlyPriceInfoString = yearlyPriceInfoString,
            onClickTosLink = onClickTosLink,
            onClickPrivacyLink = onClickPrivacyLink,
            onClickPurchase = onClickPurchase
        )
    } else {
        SuccessScreenContentLandscape(
            state = state,
            ctaString = ctaString,
            monthlyPriceInfoString = monthlyPriceInfoString,
            yearlyPriceInfoString = yearlyPriceInfoString,
            onClickTosLink = onClickTosLink,
            onClickPrivacyLink = onClickPrivacyLink,
            onClickPurchase = onClickPurchase
        )
    }
}

@Composable
private fun SuccessScreenContentPortrait(
    state: OfferDetailsViewState.Success,
    ctaString: (OfferDetails.PriceInfo?) -> String?,
    monthlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    yearlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    val offerDetails = state.viewData.offerDetails ?: return
    val benefits = offerDetails.benefits
    Column(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .fillMaxSize()
    ) {
        if (offerDetails.warning != null) {
            Spacer(modifier = Modifier.size(16.dp))
            InfoboxMedium(title = getBenefitString(offerDetails.warning), mood = Mood.Warning)
        }
        BenefitsList(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .weight(1f),
            benefits = benefits
        )
        Spacer(modifier = Modifier.size(14.dp))
        DisclaimerContent(
            onClickTosLink = onClickTosLink,
            onClickPrivacyLink = onClickPrivacyLink
        )
        Spacer(modifier = Modifier.size(8.dp))
        PurchaseBottomBar(
            offerDetails = offerDetails,
            ctaString = ctaString,
            monthlyPriceInfoString = monthlyPriceInfoString,
            yearlyPriceInfoString = yearlyPriceInfoString,
            onClickPurchase = onClickPurchase
        )
    }
}

@Composable
private fun SuccessScreenContentLandscape(
    state: OfferDetailsViewState.Success,
    ctaString: (OfferDetails.PriceInfo?) -> String?,
    monthlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    yearlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    val offerDetails = state.viewData.offerDetails ?: return
    val benefits = offerDetails.benefits
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .widthIn(max = 640.dp)
        ) {
            if (offerDetails.warning != null) {
                item {
                    Spacer(modifier = Modifier.size(16.dp))
                    InfoboxMedium(
                        title = getBenefitString(offerDetails.warning),
                        mood = Mood.Warning
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.size(4.dp))
            }
            items(benefits) {
                BenefitRow(modifier = Modifier.padding(top = 12.dp), item = it)
            }
            item {
                Spacer(modifier = Modifier.size(24.dp))
                DisclaimerContent(
                    onClickTosLink = onClickTosLink,
                    onClickPrivacyLink = onClickPrivacyLink
                )
            }
            item {
                PurchaseBottomBarLandscape(
                    offerDetails = offerDetails,
                    ctaString = ctaString,
                    monthlyPriceInfoString = monthlyPriceInfoString,
                    yearlyPriceInfoString = yearlyPriceInfoString,
                    onClickPurchase = onClickPurchase
                )
            }
        }
    }
}

@Composable
private fun PurchaseBottomBar(
    offerDetails: OfferDetails,
    ctaString: (OfferDetails.PriceInfo?) -> String?,
    monthlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    yearlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val monthlyProduct = offerDetails.monthlyProduct
        val yearlyProduct = offerDetails.yearlyProduct
        PurchaseButton(
            modifier = Modifier.fillMaxWidth(),
            product = monthlyProduct,
            ctaString = ctaString(monthlyProduct?.priceInfo),
            priceInfoString = monthlyPriceInfoString(monthlyProduct?.priceInfo),
            isPrimary = false,
            onClickPurchase = onClickPurchase
        )
        PurchaseButton(
            modifier = Modifier.fillMaxWidth(),
            product = yearlyProduct,
            ctaString = ctaString(yearlyProduct?.priceInfo),
            priceInfoString = yearlyPriceInfoString(yearlyProduct?.priceInfo),
            isPrimary = true,
            onClickPurchase = onClickPurchase
        )
    }
}

@Composable
private fun PurchaseBottomBarLandscape(
    modifier: Modifier = Modifier,
    offerDetails: OfferDetails,
    ctaString: (OfferDetails.PriceInfo?) -> String?,
    monthlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    yearlyPriceInfoString: (OfferDetails.PriceInfo?) -> String?,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    val monthlyProduct = offerDetails.monthlyProduct
    val yearlyProduct = offerDetails.yearlyProduct

    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
        maxItemsInEachRow = 2
    ) {
        PurchaseButton(
            modifier = modifier,
            product = monthlyProduct,
            ctaString = ctaString(monthlyProduct?.priceInfo),
            priceInfoString = monthlyPriceInfoString(monthlyProduct?.priceInfo),
            isPrimary = false,
            onClickPurchase = onClickPurchase
        )
        PurchaseButton(
            modifier = modifier,
            product = yearlyProduct,
            ctaString = ctaString(yearlyProduct?.priceInfo),
            priceInfoString = yearlyPriceInfoString(yearlyProduct?.priceInfo),
            isPrimary = true,
            onClickPurchase = onClickPurchase
        )
    }
}

@Composable
private fun DisclaimerContent(
    modifier: Modifier = Modifier,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(
        modifier = modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp),
        horizontalAlignment = if (portrait) Alignment.Start else Alignment.End
    ) {
        Text(
            style = DashlaneTheme.typography.bodyReducedRegular,
            text = if (portrait) {
                stringResource(id = R.string.plan_disclaimer_default_v2)
            } else {
                stringResource(id = R.string.plan_disclaimer_default_v2_long)
            },
            textAlign = if (portrait) {
                TextAlign.Start
            } else {
                TextAlign.End
            }
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            maxItemsInEachRow = 2
        ) {
            LinkButton(
                text = stringResource(id = R.string.plan_disclaimer_legal_arg_privacy_policy),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onClickPrivacyLink
            )
            LinkButton(
                text = stringResource(id = R.string.plan_disclaimer_legal_arg_terms_of_service),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onClickTosLink
            )
        }
    }
}

@Composable
private fun PurchaseButton(
    modifier: Modifier = Modifier,
    product: OfferDetails.Product?,
    ctaString: String?,
    priceInfoString: String?,
    isPrimary: Boolean,
    onClickPurchase: (OfferDetails.Product) -> Unit
) {
    if (product != null && ctaString != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ButtonLarge(
                modifier = modifier,
                layout = ButtonLayout.TextOnly(text = ctaString),
                onClick = { onClickPurchase(product) },
                mood = Mood.Brand,
                intensity = if (isPrimary) Intensity.Catchy else Intensity.Quiet,
                enabled = product.enabled,
            )

            if (priceInfoString != null) {
                Text(
                    modifier = modifier,
                    text = priceInfoString,
                    style = DashlaneTheme.typography.bodyHelperRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BenefitsList(
    modifier: Modifier = Modifier,
    benefits: List<TextResource>
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(benefits) {
            BenefitRow(item = it)
        }
    }
}

@Composable
private fun BenefitRow(modifier: Modifier = Modifier, item: TextResource) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            token = IconTokens.checkmarkOutlined,
            contentDescription = null,
            tint = DashlaneTheme.colors.textBrandStandard,
            modifier = Modifier
                .width(28.dp)
                .height(26.dp)
                .background(
                    color = DashlaneTheme.colors.containerExpressiveBrandQuietIdle,
                    shape = RoundedCornerShape(size = 4.dp)
                )
                .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
        )
        HtmlText(
            modifier = Modifier.padding(start = 16.dp),
            htmlText = getBenefitString(item),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard
        )
    }
}

@SuppressWarnings("SpreadOperator")
@Composable
private fun getBenefitString(text: TextResource): String {
    return when (text) {
        is TextResource.StringText -> stringResource(
            text.stringRes,
            *text.args.map { it.format() }.toTypedArray()
        )
        is TextResource.PluralsText -> pluralStringResource(
            text.pluralsRes,
            text.quantity,
            *text.args.map { it.format() }.toTypedArray()
        )
    }
}

@Composable
private fun TextResource.Arg.format(): Any = when (this) {
    is TextResource.Arg.StringArg -> arg
    is TextResource.Arg.StringResArg -> stringResource(arg)
    is TextResource.Arg.IntArg -> arg
}
