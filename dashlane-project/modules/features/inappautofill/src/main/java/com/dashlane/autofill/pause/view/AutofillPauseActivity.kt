package com.dashlane.autofill.pause.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.pause.AskPauseViewModel
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.autofill.util.setFieldCompat
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class AutofillPauseActivity :
    CoroutineScope,
    AutoFillResponseActivity() {
    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private val viewModel by viewModels<AskPauseViewModel>()

    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                AskPauseBottomSheetScreen(
                    viewModel = viewModel,
                    onPauseSuccess = ::onPauseSuccess,
                    onPauseError = ::onPauseError,
                    onPauseCancelled = ::onPauseCancelled,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val summary = summary
        if (summary == null) {
            finish()
            return
        }

        viewModel.load(summary.formSource)
    }

    fun onPauseSuccess(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
        finishWithPauseResponse()
    }

    fun onPauseError(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
        finishWithAutoFillSuggestions()
    }

    fun onPauseCancelled() {
        finishWithAutoFillSuggestions()
    }

    private fun finishWithPauseResponse() {
        this.launch(Dispatchers.Main) {
            finishWithResultIntentResult(buildPauseResponse(summary))
        }
    }

    private fun buildPauseResponse(summary: AutoFillHintSummary?): FillResponse =
        FillResponse.Builder().apply {
            summary?.entries?.mapNotNull { entry ->
                entry.id?.let { buildEmptyDataset(it) }
            }?.forEach {
                addDataset(it)
            }
        }.build()

    private fun buildEmptyDataset(autofillId: AutofillId): Dataset =
        Dataset.Builder().setFieldCompat(autofillId, AutofillValue.forText("")).build()

    companion object {
        private const val EXTRA_HAD_CREDENTIALS = "extra_had_credentials"

        internal fun getAuthIntentSenderForPause(
            context: Context,
            summary: AutoFillHintSummary,
            hadCredentials: Boolean,
        ): IntentSender {
            val intent = createIntent(
                context,
                summary,
                AutofillPauseActivity::class,
            )
            intent.putExtra(EXTRA_HAD_CREDENTIALS, hadCredentials)
            return createIntentSender(context, intent)
        }
    }
}