package com.dashlane.autofill.frozenautofill

import android.content.Context
import android.content.IntentSender
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.design.theme.DashlaneTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FrozenAutofillActivity : AutoFillResponseActivity() {

    @Inject
    lateinit var autofillNavigationService: AutofillNavigationService

    private val viewModel: FrozenAutofillViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        setContent {
            DashlaneTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = viewModel) {
                    viewModel.onViewReady()
                }

                FrozenAutofillBottomSheet(
                    state = uiState.value,
                    onCancelClick = ::onNotNowClick,
                    onUpgradeClick = ::onUpgradeClick
                )
            }
        }
    }

    private fun onNotNowClick() {
        viewModel.onNotNowClicked()
        onFinish()
    }

    private fun onUpgradeClick() {
        viewModel.onUnfreezeAccountClicked()
        autofillNavigationService.navigateToFrozenAccountPaywall(this)
        onFinish()
    }

    private fun onFinish() {
        finishWithResult(
            itemToFill = null,
            matchType = matchType
        )
    }

    companion object {
        fun getPendingIntent(
            context: Context,
            summary: AutoFillHintSummary,
        ): IntentSender {
            val intent = createIntent(context, summary, FrozenAutofillActivity::class)
            return createIntentSender(
                context,
                intent
            )
        }
    }
}