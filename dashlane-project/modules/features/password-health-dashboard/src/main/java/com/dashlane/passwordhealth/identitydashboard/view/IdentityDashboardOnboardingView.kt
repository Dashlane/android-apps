package com.dashlane.passwordhealth.identitydashboard.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.compat.view.ComposeViewWithPreview
import com.dashlane.design.component.compat.view.observableProperty
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.passwordhealth.R
import com.dashlane.ui.common.compose.components.IntroScreenContent

class IdentityDashboardOnboardingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ComposeViewWithPreview(context, attrs, defStyleAttr) {
    var onAddLoginClick: () -> Unit by observableProperty {
        
    }

    @Composable
    override fun CreateContent() {
        DashlaneTheme {
            IntroScreenContent(
                contentPadding = PaddingValues(16.dp),
                iconToken = IconTokens.featurePasswordHealthOutlined,
                title = stringResource(R.string.password_health_onboarding_empty_screen_title),
                items = listOf(
                    IntroScreenContent.Item(
                        icon = IconTokens.itemLoginOutlined,
                        title = stringResource(R.string.password_health_onboarding_empty_screen_item_1_title),
                        description = stringResource(R.string.password_health_onboarding_empty_screen_item_1_description),
                    ),
                    IntroScreenContent.Item(
                        icon = IconTokens.tipOutlined,
                        title = stringResource(R.string.password_health_onboarding_empty_screen_item_2_title),
                        description = stringResource(R.string.password_health_onboarding_empty_screen_item_2_description),
                    ),
                    IntroScreenContent.Item(
                        icon = IconTokens.healthPositiveOutlined,
                        title = stringResource(R.string.password_health_onboarding_empty_screen_item_3_title),
                        description = stringResource(R.string.password_health_onboarding_empty_screen_item_3_description),
                    ),
                ),
                action = IntroScreenContent.Action(
                    text = stringResource(R.string.password_health_onboarding_empty_screen_add_login_button),
                    onClick = onAddLoginClick,
                ),
            )
        }
    }
}