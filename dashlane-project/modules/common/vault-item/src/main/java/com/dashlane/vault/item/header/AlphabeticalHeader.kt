package com.dashlane.vault.item.header

import com.dashlane.vault.item.comparator.getComparableField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import java.util.Locale

fun SummaryObject.getAlphabeticalHeader(identityNameHolderService: IdentityNameHolderService): String {
    val text = getComparableField(identityNameHolderService)
    val emptyTextCategory = "..."
    val firstLetter = try {
        text?.substring(0, 1)?.uppercase(Locale.US)?.takeUnless { it.isEmpty() }
    } catch (e: Exception) {
        null
    } ?: " "

    return when (firstLetter[0]) {
        ' ' -> emptyTextCategory
        in '0'..'9' -> "0-9"
        else -> firstLetter
    }
}