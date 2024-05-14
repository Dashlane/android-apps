package com.dashlane.autofill.util

import android.service.autofill.SaveInfo
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal class SaveInfoWrapper(summary: AutoFillHintSummary) {

    val saveInfo: SaveInfo? = when {
        
        BrowserDetectionHelper.isBrowserSupported(summary.packageName) -> null
        summary.formType == AutoFillFormType.CREDENTIAL -> buildForCredential(summary)
        summary.formType == AutoFillFormType.CREDIT_CARD -> buildForCreditCard(summary)
        else -> null 
    }

    fun hasSaveInfo(): Boolean {
        return saveInfo != null
    }

    private fun buildForCreditCard(summary: AutoFillHintSummary): SaveInfo? {
        return buildWith(
            SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD,
            summary,
            arrayOf(
                arrayOf(
                    AutoFillHint.CREDIT_CARD_EXPIRATION_DATE,
                    AutoFillHint.CREDIT_CARD_EXPIRATION_DAY,
                    AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH,
                    AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR
                ),
                arrayOf(AutoFillHint.CREDIT_CARD_NUMBER),
                arrayOf(AutoFillHint.CREDIT_CARD_SECURITY_CODE)
            )
        )
    }

    private fun buildForCredential(summary: AutoFillHintSummary): SaveInfo? {
        return buildWith(
            SaveInfo.SAVE_DATA_TYPE_PASSWORD,
            summary,
            
            arrayOf(
                arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD),
                arrayOf(AutoFillHint.USERNAME, AutoFillHint.EMAIL_ADDRESS)
            )
        )
    }

    private fun buildWith(
        saveType: Int,
        summary: AutoFillHintSummary,
        hintsRequireGroups: Array<Array<String>>
    ): SaveInfo? {
        val requiredIdsByGroup = hintsRequireGroups.flatMap { hints ->
            
            val requiredIds = summary.entries.filter { it.hasOneOfHints(hints) }.map { it.id }
            if (requiredIds.isEmpty()) {
                
                return null
            }
            requiredIds
        }
        if (requiredIdsByGroup.isEmpty()) {
            return null
        }
        return SaveInfo.Builder(saveType, requiredIdsByGroup.toTypedArray()).apply {
            if (saveType != SaveInfo.SAVE_DATA_TYPE_USERNAME && saveType != SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS) {
                
                setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
            }
        }.build()
    }
}