package com.dashlane.autofill.request.save

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.service.autofill.SaveRequest
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.autofill.util.AutofillLogUtil
import com.dashlane.autofill.util.DomainWrapper
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.SaveType
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.vault.model.VaultItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SaveRequestActivity : AutoFillResponseActivity(), CoroutineScope, SaveCallback {

    @Inject
    lateinit var saveRequestLogger: AutofillSaveRequestLogger

    private var itemType: ItemType? = null
    lateinit var domainWrapper: DomainWrapper

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    override fun onResume() {
        super.onResume()
        val saveRequest = intent.getParcelableExtraCompat<SaveRequest>(SAVE_REQUEST)
        val saveSummary = summary
        itemType = summary?.getItemType()
        domainWrapper = AutofillLogUtil.extractDomainFrom(
            summary?.webDomain?.toUrlDomainOrNull(),
            summary?.packageName
        )
        if (saveRequest == null || saveSummary == null || itemType == null) {
            finish()
            return
        }

        performLoginAndUnlock {
            handleSaveRequest(saveRequest, saveSummary)
        }
    }

    private fun handleSaveRequest(request: SaveRequest, saveSummary: AutoFillHintSummary) {
        componentInternal.saveRequestHandler.onSaveRequest(
            context = this,
            coroutineScope = this,
            clientState = request.clientState,
            summary = saveSummary,
            saveCallback = this,
            forKeyboard = false
        )
    }

    private fun AutoFillHintSummary.getItemType() = when (this.formType) {
        AutoFillFormType.CREDENTIAL -> ItemType.CREDENTIAL
        AutoFillFormType.CREDIT_CARD -> ItemType.CREDIT_CARD
        else -> null
    }

    override fun onSuccess(isUpdate: Boolean, vaultItem: VaultItem<*>) {
        val saveType = if (isUpdate) {
            SaveType.REPLACE
        } else {
            SaveType.SAVE
        }

        itemType?.let {
            saveRequestLogger.onSave(it, saveType, domainWrapper, vaultItem)
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onFailure(message: CharSequence?) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {
        const val SAVE_REQUEST = "save_request"

        internal fun getSaveRequestIntent(
            context: Context,
            summary: AutoFillHintSummary,
            saveRequest: SaveRequest
        ): Intent {
            val intent =
                createIntent(
                    context,
                    summary,
                    SaveRequestActivity::class
                )
            intent.putExtra(SAVE_REQUEST, saveRequest)
            return intent
        }

        internal fun getSaveRequestPendingIntent(
            context: Context,
            summary: AutoFillHintSummary,
            saveRequest: SaveRequest
        ): IntentSender = createIntentSender(
            context,
            getSaveRequestIntent(context, summary, saveRequest)
        )
    }
}