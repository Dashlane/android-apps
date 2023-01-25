package com.dashlane.autofill.api.unlockfill

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.request.autofill.database.ItemLoader
import com.dashlane.autofill.api.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

internal class AuthentifiantUnlocker(
    private val unlockAuthentifiantView: UnlockAuthentifiantView,
    private val itemLoader: ItemLoader,
    private val lockManager: AutofillAnalyzerDef.ILockManager,
    private val autofillUsageLog: AutofillAnalyzerDef.IAutofillUsageLog,
    private val forKeyboard: Boolean
) {
    fun unlockAuthentifiant(itemId: String?, formSource: AutoFillFormSource) {
        val itemToFill = itemLoader.loadAuthentifiant(itemId)
        if (itemToFill == null) {
            unlockAuthentifiantView.finishWithAutoFillSuggestions()
            return
        }
        val unlockedAuthentifiant = UnlockedAuthentifiant(formSource, itemToFill)

        if (lockManager.isInAppLoginLocked) {
            if (unlockAuthentifiantView.canRequestLockScreen()) {
                unlockAuthentifiantView.startLockActivity()
                autofillUsageLog.onClickToAutoFillCredentialButLock(
                    getAutofillApiOrigin(forKeyboard),
                    unlockedAuthentifiant.itemUrl
                )
            } else {
                unlockAuthentifiantView.finish()
            }
        } else {
            
            if (unlockAuthentifiantView.isFirstRun()) {
                autofillUsageLog.onClickToAutoFillCredentialNotLock(
                    getAutofillApiOrigin(forKeyboard),
                    unlockedAuthentifiant.packageName,
                    unlockedAuthentifiant.itemUrl
                )
            }
            unlockAuthentifiantView.authentifiantItemUnlocked(unlockedAuthentifiant)
        }
    }
}
