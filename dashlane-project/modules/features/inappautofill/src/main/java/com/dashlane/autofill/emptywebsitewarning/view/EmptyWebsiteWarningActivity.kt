package com.dashlane.autofill.emptywebsitewarning.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.emptywebsitewarning.EmptyWebsiteWarningViewModel
import com.dashlane.autofill.emptywebsitewarning.ui.EmptyWebsiteBottomSheetScreen
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.toUrlDomain
import com.dashlane.util.Toaster
import com.dashlane.util.tryOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class EmptyWebsiteWarningActivity : AutoFillResponseActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private val viewModel by viewModels<EmptyWebsiteWarningViewModel>()

    @Inject
    lateinit var toaster: Toaster

    private lateinit var summaryWebDomain: String

    private lateinit var itemId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        summaryWebDomain = tryOrNull { summary?.webDomain?.toUrlDomain()?.root?.value } ?: ""
        itemId = intent.getStringExtra(EXTRA_SELECTED_ITEM_ID) ?: ""
        setContent {
            DashlaneTheme {
                EmptyWebsiteBottomSheetScreen(
                    viewModel = viewModel,
                    onAddWebsiteSuccess = ::onAddWebsiteSuccess,
                    onAddWebsiteError = ::onAddWebsiteError,
                    onCancel = ::onCancel,
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
        performLoginAndUnlock {
            viewModel.load(websiteToAdd = summaryWebDomain, itemId = itemId)
        }
    }

    fun onAddWebsiteSuccess(message: String, itemToFill: AuthentifiantItemToFill) {
        toaster.show(message, Toast.LENGTH_SHORT)
        finishWithResult(
            itemToFill = itemToFill,
            matchType = matchType,
        )
    }

    fun onAddWebsiteError() {
        toaster.show(R.string.error, Toast.LENGTH_SHORT)
        finishWithResult(
            itemToFill = null,
            matchType = matchType,
        )
    }

    fun onCancel() {
        finishWithResult(
            itemToFill = null,
            matchType = matchType,
        )
    }

    companion object {
        const val EXTRA_SELECTED_ITEM_ID = "selected_item_id"

        internal fun getAuthIntentSenderForEmptyWebsiteWarning(
            context: Context,
            itemId: String,
            summary: AutoFillHintSummary,
            matchType: MatchType,
        ): IntentSender {
            val intent = createIntent(context, summary, EmptyWebsiteWarningActivity::class)
            intent.putExtra(EXTRA_SELECTED_ITEM_ID, itemId)
            intent.putExtra(EXTRA_MATCH_TYPE, matchType.code)
            return createIntentSender(context, intent)
        }
    }
}