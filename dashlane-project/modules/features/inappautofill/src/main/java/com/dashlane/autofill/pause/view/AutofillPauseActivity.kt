package com.dashlane.autofill.pause.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.ui.AutoFillResponseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class AutofillPauseActivity :
    CoroutineScope,
    AskPauseDialogContract,
    AutoFillResponseActivity() {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private lateinit var summaryPackageName: String
    private lateinit var summaryWebDomain: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        summaryPackageName = summary?.packageName ?: ""
        summaryWebDomain = summary?.webDomain ?: ""
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }

        openBottomSheetAuthentifiantsListDialog()
    }

    private fun openBottomSheetAuthentifiantsListDialog() {
        var dialog = supportFragmentManager.findFragmentByTag(BottomSheetAskPauseDialogFragment.PAUSE_DIALOG_TAG)
        if (dialog == null) {
            dialog = BottomSheetAskPauseDialogFragment.buildFragment()
            dialog.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
            dialog.show(supportFragmentManager, BottomSheetAskPauseDialogFragment.PAUSE_DIALOG_TAG)
        }
    }

    override fun getPausedFormSource(): AutoFillFormSource {
        return summary!!.formSource
    }

    override fun onPauseFormSourceDialogResponse(pauseDurations: PauseDurations?) {
        pauseDurations?.let {
            finishWithPauseResponse()
        } ?: finishWithAutoFillSuggestions()
    }

    private fun finishWithPauseResponse() {
        this.launch(Dispatchers.Main) {
            finishWithResultIntentResult(buildPauseResponse(summary))
        }
    }

    private fun buildPauseResponse(summary: AutoFillHintSummary?): FillResponse {
        return FillResponse.Builder().apply {
            summary?.entries?.mapNotNull { entry ->
                entry.id?.let { buildEmptyDataset(it) }
            }?.forEach {
                addDataset(it)
            }
        }.build()
    }

    private fun buildEmptyDataset(autofillId: AutofillId): Dataset {
        return buildEmptyDatasetBuilder().apply {
            setValue(autofillId, AutofillValue.forText(""))
        }.build()
    }

    @Suppress("DEPRECATION")
    private fun buildEmptyDatasetBuilder(): Dataset.Builder {
        return Dataset.Builder()
    }

    companion object {
        private const val EXTRA_HAD_CREDENTIALS = "extra_had_credentials"

        internal fun getAuthIntentSenderForPause(
            context: Context,
            summary: AutoFillHintSummary,
            hadCredentials: Boolean
        ): IntentSender {
            val intent = createIntent(
                context,
                summary,
                AutofillPauseActivity::class
            )
            intent.putExtra(EXTRA_HAD_CREDENTIALS, hadCredentials)
            return createIntentSender(context, intent)
        }
    }
}
