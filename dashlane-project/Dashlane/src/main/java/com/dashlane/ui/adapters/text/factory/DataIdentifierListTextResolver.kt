package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil
import javax.inject.Inject

open class DataIdentifierListTextResolver @Inject constructor(private val identityUtil: IdentityUtil) {

    fun getLine1(context: Context, item: SummaryObject): StatusText {
        return getTextFactory(context, item).getLine1()
    }

    fun getLine2(context: Context, item: SummaryObject): StatusText {
        return getTextFactory(context, item).getLine2(StatusText(context.getString(R.string.incomplete), true))
    }

    internal fun getTextFactory(context: Context, item: SummaryObject): DataIdentifierListTextFactory {
        return when (item) {
            is SummaryObject.Address -> AddressListTextFactory(context, item)
            is SummaryObject.Authentifiant -> AuthentifiantListTextFactory(context, item)
            is SummaryObject.BankStatement -> BankStatementListTextFactory(context, item)
            is SummaryObject.Company -> CompanyListTextFactory(context, item)
            is SummaryObject.DriverLicence -> DriverLicenceListTextFactory(context, item, identityUtil)
            is SummaryObject.Email -> EmailListTextFactory(context, item)
            is SummaryObject.FiscalStatement -> FiscalStatementListTextFactory(context, item)
            is SummaryObject.IdCard -> IdCardListTextFactory(context, item, identityUtil)
            is SummaryObject.Identity -> IdentityListTextFactory(context, item)
            is SummaryObject.Passport -> PassportListTextFactory(context, item, identityUtil)
            is SummaryObject.PaymentCreditCard -> PaymentCreditCardListTextFactory(context, item)
            is SummaryObject.PaymentPaypal -> PaymentPaypalListTextFactory(context, item)
            is SummaryObject.PersonalWebsite -> PersonalWebsiteListTextFactory(context, item)
            is SummaryObject.Phone -> PhoneListTextFactory(context, item)
            is SummaryObject.SecureNote -> SecureNoteListTextFactory(context, item)
            is SummaryObject.SocialSecurityStatement -> SocialSecurityStatementListTextFactory(
                context,
                item,
                identityUtil
            )
            else -> DefaultResult(context)
        }
    }

    private class DefaultResult(private val context: Context) : DataIdentifierListTextFactory {

        override fun getLine1(): StatusText {
            if (DeveloperUtilities.systemIsInDebug(context)) {
                return StatusText(
                    "TODO: ListTextFactory missing",
                    true
                )
            }
            return StatusText("", false)
        }

        override fun getLine2(default: StatusText): StatusText {
            if (DeveloperUtilities.systemIsInDebug(context)) {
                return StatusText(
                    "TODO: No ListTextFactory in DataIdentifierListTextResolver",
                    true
                )
            }
            return StatusText("", false)
        }

        override fun getLine2FromField(field: SearchField<*>): StatusText {
            if (DeveloperUtilities.systemIsInDebug(context)) {
                return StatusText(
                    "TODO: No ListTextFactory in DataIdentifierListTextResolver",
                    true
                )
            }
            return StatusText("", false)
        }
    }
}
