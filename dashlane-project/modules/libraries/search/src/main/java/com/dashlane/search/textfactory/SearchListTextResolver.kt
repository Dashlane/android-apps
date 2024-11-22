package com.dashlane.search.textfactory

import android.content.Context
import com.dashlane.search.SearchField
import com.dashlane.util.ignoreEscapedCharacter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.util.BankDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SearchListTextResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    private val bankDataProvider: BankDataProvider,
) : DataIdentifierListTextResolver by dataIdentifierListTextResolver {
    fun getHighlightedLine1(
        item: SummaryObject,
        targetText: String
    ): StatusText {
        val primaryLine = dataIdentifierListTextResolver.getLine1(item)
        return StatusText(
            text = primaryLine.text,
            isWarning = primaryLine.isWarning,
            textToHighlight = targetText
        )
    }

    fun getHighlightedLine2(
        item: SummaryObject,
        targetText: String,
        searchField: SearchField<*>
    ): StatusText {
        val secondaryLine = getTextFactory(item).getLine2FromField(searchField)
            ?: return dataIdentifierListTextResolver.getLine2(item)

        return StatusText(
            text = secondaryLine.text.ignoreEscapedCharacter().focusOn(targetText),
            isWarning = secondaryLine.isWarning,
            textToHighlight = targetText
        )
    }

    private fun getTextFactory(item: SummaryObject): DataIdentifierSearchListTextFactory =
        when (item) {
            is SummaryObject.Address -> AddressSearchListTextFactory(item)
            is SummaryObject.Authentifiant -> AuthentifiantSearchListTextFactory(item)
            is SummaryObject.BankStatement -> BankStatementSearchListTextFactory(item)
            is SummaryObject.Company -> CompanySearchListTextFactory(item)
            is SummaryObject.DriverLicence -> DriverLicenceListTextFactory(item)
            is SummaryObject.Email -> EmailSearchListTextFactory(item)
            is SummaryObject.IdCard -> IdCardSearchListTextFactory(item)
            is SummaryObject.Identity -> IdentitySearchListTextFactory(item)
            is SummaryObject.Passkey -> PasskeySearchListTextFactory(item)
            is SummaryObject.Passport -> PassportSearchListTextFactory(item)
            is SummaryObject.PaymentCreditCard -> PaymentCreditCardSearchListTextFactory(item, bankDataProvider)
            is SummaryObject.PersonalWebsite -> PersonalWebsiteSearchListTextFactory(item)
            is SummaryObject.SecureNote -> SecureNoteSearchListTextFactory(context, item)
            is SummaryObject.SocialSecurityStatement -> SocialSecurityStatementSearchListTextFactory(context, item)
            is SummaryObject.AuthCategory,
            is SummaryObject.Collection,
            is SummaryObject.DataChangeHistory,
            is SummaryObject.FiscalStatement,
            is SummaryObject.GeneratedPassword,
            is SummaryObject.SecureFileInfo,
            is SummaryObject.SecureNoteCategory,
            is SummaryObject.SecurityBreach,
            is SummaryObject.Phone,
            is SummaryObject.PaymentPaypal -> DefaultSearchListTextFactory()
            else -> DefaultSearchListTextFactory()
        }

    private fun String.focusOn(targetText: String): String {
        val startIndex = this.indexOf(targetText, ignoreCase = true)
        if (startIndex <= PREFIX_CROPPING_THRESHOLD) return this

        return "..." + this.substring(startIndex, this.length)
    }

    companion object {
        private const val PREFIX_CROPPING_THRESHOLD = 30
    }
}