package com.dashlane.autofill.api.emptywebsitewarning.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.emptywebsitewarning.domain.EmptyWebsiteWarningDialogResponse
import com.dashlane.autofill.api.model.toItemToFill
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.toUrlDomain
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class EmptyWebsiteWarningActivity :
    AutoFillResponseActivity(),
    EmptyWebsiteWarningDialogResponse,
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private lateinit var summaryWebDomain: String

    private lateinit var itemId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        summaryWebDomain = tryOrNull { summary?.webDomain?.toUrlDomain()?.root?.value } ?: ""
        itemId = intent.getStringExtra(EXTRA_SELECTED_ITEM_ID) ?: ""
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }
        performLoginAndUnlock {
            openWarningBottomSheetDialog()
        }
    }

    override fun onAutofillResult(result: VaultItem<SyncObject.Authentifiant>) = finishWithResult(
        itemToFill = result.toItemToFill(),
        autofillFeature = AutofillFeature.EMPTY_WEBSITE,
        matchType = matchType
    )

    override fun onNoResult() = finishWithResult(
        itemToFill = null,
        autofillFeature = AutofillFeature.EMPTY_WEBSITE,
        matchType = matchType
    )

    private fun openWarningBottomSheetDialog() {
        var dialog = supportFragmentManager.findFragmentByTag(EMPTY_WEBSITE_WARNING_DIALOG)
        if (dialog == null) {
            dialog = BottomSheetEmptyWebsiteWarningDialogFragment.create(summaryWebDomain, itemId)
            dialog.setStyle(
                DialogFragment.STYLE_NO_FRAME,
                R.style.Theme_Dashlane_Transparent_Cancelable
            )
            dialog.show(supportFragmentManager, EMPTY_WEBSITE_WARNING_DIALOG)
        }
    }

    companion object {

        const val EMPTY_WEBSITE_WARNING_DIALOG = "empty_website_warning_dialog"
        const val EXTRA_SELECTED_ITEM_ID = "selected_item_id"

        internal fun getAuthIntentSenderForEmptyWebsiteWarning(
            context: Context,
            itemId: String,
            summary: AutoFillHintSummary,
            matchType: MatchType
        ): IntentSender {
            val intent = createIntent(context, summary, EmptyWebsiteWarningActivity::class)
            intent.putExtra(EXTRA_SELECTED_ITEM_ID, itemId)
            intent.putExtra(EXTRA_MATCH_TYPE, matchType.code)
            return createIntentSender(context, intent)
        }
    }
}
