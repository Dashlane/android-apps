package com.dashlane.vpn.thirdparty.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLargeBar
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Card
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.HorizontalDivider
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.PasswordDisplayField
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.screentemplate.BoxColumn
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.vpn.thirdparty.R
import com.dashlane.vpn.thirdparty.VpnThirdPartyData
import com.dashlane.vpn.thirdparty.VpnThirdPartyData.ActionType.DOWNLOAD_APP
import com.dashlane.vpn.thirdparty.VpnThirdPartyData.ActionType.LAUNCH_APP

private const val HOTSPOT_URL = "https://hotspotshield.com"

@Suppress("LongMethod", "kotlin:S107")
@Composable
internal fun VpnAccountContent(
    login: String,
    password: String,
    actionType: VpnThirdPartyData.ActionType,
    onLoginCopyClick: () -> Unit,
    onPasswordCopyClick: () -> Unit,
    onShowFaqClick: () -> Unit,
    onDownloadHotspotShieldClick: () -> Unit,
    onLaunchHotspotShieldClick: () -> Unit,
) {
    var obfuscatedPasswordField by rememberSaveable { mutableStateOf(true) }
    BoxColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        ThumbnailDomainIcon(
            urlDomain = HOTSPOT_URL,
            size = ThumbnailSize.XLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.semantics { heading() },
            text = stringResource(R.string.vpn_third_party_all_set_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.vpn_third_party_all_set_subtitle),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column {
                DisplayField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.vpn_third_party_account_login),
                    value = login,
                    actions = DisplayFieldActions.newInstance(
                        action1 = getCopyAction(onLoginCopyClick),
                    ),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                PasswordDisplayField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.vpn_third_party_account_password),
                    revealedValue = password,
                    obfuscated = obfuscatedPasswordField,
                    isEmpty = password.isEmpty(),
                    action1 = FieldAction.HideReveal(
                        resources = LocalContext.current.resources,
                        onClick = {
                            obfuscatedPasswordField = !obfuscatedPasswordField
                            true
                        },
                    ),
                    action2 = getCopyAction(onPasswordCopyClick),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinkButton(
            text = stringResource(R.string.vpn_third_party_faq_title),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = onShowFaqClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1.0f))
        when (actionType) {
            DOWNLOAD_APP -> {
                ButtonLargeBar(
                    primaryText = stringResource(R.string.vpn_third_party_install_app_button),
                    onPrimaryButtonClick = onDownloadHotspotShieldClick,
                    secondaryText = null,
                    onSecondaryButtonClick = null,
                )
            }

            LAUNCH_APP -> {
                ButtonLargeBar(
                    primaryText = stringResource(R.string.vpn_third_party_launch_app_button),
                    onPrimaryButtonClick = onLaunchHotspotShieldClick,
                    secondaryText = null,
                    onSecondaryButtonClick = null,
                )
            }
        }
    }
}

@Composable
private fun getCopyAction(onClick: () -> Unit): FieldAction.Generic = FieldAction.Generic(
    iconLayout = ButtonLayout.IconOnly(
        iconToken = IconTokens.actionCopyOutlined,
        contentDescription = stringResource(R.string.copy),
    ),
    onClick = {
        onClick()
        true
    },
)

private class ActionTypeProvider : PreviewParameterProvider<VpnThirdPartyData.ActionType> {
    override val values: Sequence<VpnThirdPartyData.ActionType>
        get() = VpnThirdPartyData.ActionType.entries.asSequence()
}

@Preview
@Composable
private fun VpnAccountContentPreview(
    @PreviewParameter(ActionTypeProvider::class) actionType: VpnThirdPartyData.ActionType,
) {
    DashlanePreview {
        VpnAccountContent(
            login = "randomemail@provider.com",
            password = "my-password",
            actionType = actionType,
            onLoginCopyClick = {},
            onPasswordCopyClick = {},
            onShowFaqClick = {},
            onDownloadHotspotShieldClick = {},
            onLaunchHotspotShieldClick = {},
        )
    }
}