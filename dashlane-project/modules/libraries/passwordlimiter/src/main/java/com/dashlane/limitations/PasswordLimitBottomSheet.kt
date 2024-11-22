package com.dashlane.limitations

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.ui.R
import com.dashlane.util.setCurrentPageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordLimitBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var passwordLimitationLogger: PasswordLimitationLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setCurrentPageView(AnyPage.PAYWALL_FREE_USER_PASSWORD_LIMIT_REACHED)
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    PasswordLimitContent()
                }
            }
        }
    }

    @Composable
    fun PasswordLimitContent() {
        Box(
            modifier = Modifier.background(
                shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp),
                color = DashlaneTheme.colors.backgroundDefault
            ),
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    painter = painterResource(id = R.drawable.illu_onboarding_vault),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(top = 32.dp),
                    text = stringResource(id = R.string.password_limit_bottom_sheet_title),
                    style = DashlaneTheme.typography.titleSectionMedium,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.password_limit_bottom_sheet_message),
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                )
                ButtonMediumBar(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 32.dp)
                        .align(Alignment.End),
                    primaryText = stringResource(id = R.string.password_limit_bottom_sheet_upgrade),
                    secondaryText = stringResource(id = R.string.password_limit_bottom_sheet_cancel),
                    onPrimaryButtonClick = {
                        passwordLimitationLogger.upgradeFromBottomSheetPaywall()
                        navigator.goToOffers()
                    },
                    onSecondaryButtonClick = {
                        passwordLimitationLogger.dismissBottomSheetPaywall()
                        dismiss()
                    }
                )
            }
        }
    }

    @Preview
    @Composable
    private fun PasswordLimitContentPreview() {
        DashlanePreview { PasswordLimitContent() }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        passwordLimitationLogger.cancelBottomSheetPaywall()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        requireActivity().finish()
    }
}