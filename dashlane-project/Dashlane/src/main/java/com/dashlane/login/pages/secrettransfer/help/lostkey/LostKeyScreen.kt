package com.dashlane.login.pages.secrettransfer.help.lostkey

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.ui.widgets.compose.GenericInfoContent
import com.dashlane.util.launchUrl

@Composable
fun LostKeyScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resetAccount: () -> Unit = { context.launchUrl("https://www.dashlane.com/account/reset/info") }
    val learnMore: () -> Unit = { HelpCenterCoordinator.openLink(context, HelpCenterLink.ARTICLE_ACCOUNT_RECOVERY_OPTIONS) }

    GenericInfoContent(
        modifier = modifier,
        icon = null,
        title = stringResource(id = R.string.login_universal_d2d_lost_key_title),
        description = stringResource(id = R.string.login_universal_d2d_lost_key_description),
        textPrimary = stringResource(id = R.string.login_universal_d2d_lost_key_primary_button),
        onClickPrimary = resetAccount,
        textSecondary = stringResource(id = R.string.login_universal_d2d_lost_key_secondary_button),
        onClickSecondary = learnMore
    )
}