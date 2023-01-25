package com.dashlane.autofill.api.pause.view

import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.pause.AutofillApiPauseLogger
import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext



class AutofillPauseActivity :
    CoroutineScope,
    AskPauseDialogContract,
    AutoFillResponseActivity() {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private lateinit var summaryPackageName: String
    private lateinit var summaryWebDomain: String

    private val pauseComponent: AutofillApiPauseComponent
        get() = AutofillApiPauseComponent(this)

    private val autofillApiPauseLogger: AutofillApiPauseLogger
        get() = pauseComponent.autofillApiPauseLogger

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

        autofillApiPauseLogger.onClickPauseSuggestion(
            summaryPackageName,
            summaryWebDomain,
            intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false),
            isLoggedIn
        )

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
            logOnClickPauseOption(it)
            finishWithPauseResponse()
        } ?: finishWithAutoFillSuggestions()
    }

    private fun logOnClickPauseOption(pauseDurations: PauseDurations) {
        when (pauseDurations) {
            PauseDurations.ONE_HOUR -> {
                autofillApiPauseLogger.onClickShortPause(
                    summaryPackageName,
                    summaryWebDomain,
                    intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false),
                    isLoggedIn
                )
            }
            PauseDurations.ONE_DAY -> {
                autofillApiPauseLogger.onClickLongPause(
                    summaryPackageName,
                    summaryWebDomain,
                    intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false),
                    isLoggedIn
                )
            }
            PauseDurations.PERMANENT -> {
                autofillApiPauseLogger.onClickDefinitePause(
                    summaryPackageName,
                    summaryWebDomain,
                    intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false),
                    isLoggedIn
                )
            }
        }
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
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            
            
            
            Dataset.Builder(RemoteViews(packageName, R.layout.list_invisible_item))
        } else {
            Dataset.Builder()
        }
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
