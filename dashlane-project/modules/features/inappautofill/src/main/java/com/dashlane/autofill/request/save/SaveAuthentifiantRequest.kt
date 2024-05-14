package com.dashlane.autofill.request.save

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class SaveAuthentifiantRequest(
    summary: AutoFillHintSummary,
    usageLog: AutofillAnalyzerDef.IAutofillUsageLog,
    databaseAccess: AutofillAnalyzerDef.DatabaseAccess
) : SaveRequest(summary, usageLog, databaseAccess) {

    override fun execute(
        context: Context,
        coroutineScope: CoroutineScope,
        saveCallback: SaveCallback,
        hasInline: Boolean
    ) {
        var username: String? = null
        var password: String? = null

        summary.entries.forEach {
            if (username == null &&
                it.hasOneOfHints(arrayOf(AutoFillHint.USERNAME, AutoFillHint.EMAIL_ADDRESS))
            ) {
                username = it.autoFillValueString
            } else if (password == null && it.hasOneOfHints(
                    arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD)
                )
            ) {
                password = it.autoFillValueString
            }
        }
        save(context, coroutineScope, username, password, saveCallback, hasInline)
    }

    private fun save(
        context: Context,
        coroutineScope: CoroutineScope,
        username: String?,
        password: String?,
        saveCallback: SaveCallback,
        hasInline: Boolean
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            val authentifiantCreated = databaseAccess.saveAuthentifiant(
                context,
                summary.webDomain,
                summary.packageName,
                username,
                password
            )
            if (authentifiantCreated != null) {
                saveCallback.onSuccess(isUpdate = false, vaultItem = authentifiantCreated)
            } else {
                notifyLogout(saveCallback, hasInline)
            }
        }
    }
}
