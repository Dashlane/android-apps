package com.dashlane.autofill.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingInAppLoginDone : Fragment() {

    private val viewModel by viewModels<OnboardingInAppLoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setCurrentPageView(AnyPage.SETTINGS_CONFIRM_AUTOFILL_ACTIVATION)
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    OnboardingDoneContent()
                }
            }
        }
    }

    @Composable
    fun OnboardingDoneContent() {
        val title = stringResource(id = R.string.onboarding_in_app_login_done_title)
        val description = stringResource(id = R.string.onboarding_in_app_login_done_subtitle)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    modifier = Modifier
                        .size(96.dp)
                        .focusable(false),
                    painter = painterResource(R.drawable.ic_modal_done),
                    colorFilter = ColorFilter.tint(DashlaneTheme.colors.textBrandQuiet.value),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(top = 18.dp),
                    text = title,
                    style = DashlaneTheme.typography.titleSectionLarge,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = description,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralStandard,
                )
            }
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.End),
                onClick = {
                    viewModel.onDoneClicked()
                    requireActivity().finish()
                },
                layout = ButtonLayout.TextOnly(
                    text = stringResource(id = R.string.onboarding_in_app_login_done_positive_button),
                ),
                intensity = Intensity.Catchy,
                mood = Mood.Brand
            )
        }
    }

    @Preview
    @Composable
    private fun OnboardingDoneAutofillPreview() {
        DashlanePreview {
            OnboardingDoneContent()
        }
    }

    companion object {
        const val ARGS_ONBOARDING_TYPE = "args_onboarding_type"

        fun newInstance(): OnboardingInAppLoginDone {
            return OnboardingInAppLoginDone()
        }
    }
}