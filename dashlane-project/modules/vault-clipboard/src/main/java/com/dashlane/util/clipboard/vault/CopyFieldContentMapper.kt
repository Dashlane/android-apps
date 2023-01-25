package com.dashlane.util.clipboard.vault

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject



internal sealed class CopyFieldContentMapper<S : SummaryObject, F : SyncObject, C : Any>(
    private val summaryObjectSafeCast: (SummaryObject) -> S?,
    private val syncObjectSafeCast: (SyncObject) -> F?
) {
    abstract val hasContentInSummary: (item: S) -> Boolean
    abstract val hasContentInFull: (item: F) -> Boolean
    abstract val getContentInSummary: (item: S) -> C?
    abstract val getContentInFull: (item: F) -> C?

    fun hasContent(summaryObject: SummaryObject): Boolean {
        val typedContent = summaryObjectSafeCast(summaryObject) ?: return false

        return hasContentInSummary(typedContent)
    }

    fun hasContent(syncObject: SyncObject): Boolean {
        val typedContent = syncObjectSafeCast(syncObject) ?: return false

        return hasContentInFull(typedContent)
    }

    fun getContent(summaryObject: SummaryObject): C? {
        val typedContent = summaryObjectSafeCast(summaryObject) ?: return null

        return getContentInSummary(typedContent)
    }

    fun getContent(syncObject: SyncObject): C? {
        val typedContent = syncObjectSafeCast(syncObject) ?: return null

        return getContentInFull(typedContent)
    }

    class ContentOnlyInSyncObjectException : Exception()

    class Authentifiant<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Authentifiant) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Authentifiant) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Authentifiant) -> C?,
        override val getContentInFull: (item: SyncObject.Authentifiant) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Authentifiant, SyncObject.Authentifiant, C>(
        { it as? SummaryObject.Authentifiant },
        { it as? SyncObject.Authentifiant }
    )

    class PaymentCreditCard<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.PaymentCreditCard) -> Boolean,
        override val hasContentInFull: (item: SyncObject.PaymentCreditCard) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.PaymentCreditCard) -> C?,
        override val getContentInFull: (item: SyncObject.PaymentCreditCard) -> C?
    ) : CopyFieldContentMapper<SummaryObject.PaymentCreditCard, SyncObject.PaymentCreditCard, C>(
        { it as? SummaryObject.PaymentCreditCard },
        { it as? SyncObject.PaymentCreditCard }
    )

    class PaymentPaypal<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.PaymentPaypal) -> Boolean,
        override val hasContentInFull: (item: SyncObject.PaymentPaypal) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.PaymentPaypal) -> C?,
        override val getContentInFull: (item: SyncObject.PaymentPaypal) -> C?
    ) : CopyFieldContentMapper<SummaryObject.PaymentPaypal, SyncObject.PaymentPaypal, C>(
        { it as? SummaryObject.PaymentPaypal },
        { it as? SyncObject.PaymentPaypal }
    )

    class BankStatement<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.BankStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.BankStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.BankStatement) -> C?,
        override val getContentInFull: (item: SyncObject.BankStatement) -> C?
    ) : CopyFieldContentMapper<SummaryObject.BankStatement, SyncObject.BankStatement, C>(
        { it as? SummaryObject.BankStatement },
        { it as? SyncObject.BankStatement }
    )

    class Address<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Address) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Address) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Address) -> C?,
        override val getContentInFull: (item: SyncObject.Address) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Address, SyncObject.Address, C>(
        { it as? SummaryObject.Address },
        { it as? SyncObject.Address }
    )

    class IdCard<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.IdCard) -> Boolean,
        override val hasContentInFull: (item: SyncObject.IdCard) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.IdCard) -> C?,
        override val getContentInFull: (item: SyncObject.IdCard) -> C?
    ) : CopyFieldContentMapper<SummaryObject.IdCard, SyncObject.IdCard, C>(
        { it as? SummaryObject.IdCard },
        { it as? SyncObject.IdCard }
    )

    class Passport<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Passport) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Passport) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Passport) -> C?,
        override val getContentInFull: (item: SyncObject.Passport) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Passport, SyncObject.Passport, C>(
        { it as? SummaryObject.Passport },
        { it as? SyncObject.Passport }
    )

    class DriverLicence<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.DriverLicence) -> Boolean,
        override val hasContentInFull: (item: SyncObject.DriverLicence) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.DriverLicence) -> C?,
        override val getContentInFull: (item: SyncObject.DriverLicence) -> C?
    ) : CopyFieldContentMapper<SummaryObject.DriverLicence, SyncObject.DriverLicence, C>(
        { it as? SummaryObject.DriverLicence },
        { it as? SyncObject.DriverLicence }
    )

    class SocialSecurityStatement<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.SocialSecurityStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.SocialSecurityStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.SocialSecurityStatement) -> C?,
        override val getContentInFull: (item: SyncObject.SocialSecurityStatement) -> C?
    ) : CopyFieldContentMapper<SummaryObject.SocialSecurityStatement, SyncObject.SocialSecurityStatement, C>(
        { it as? SummaryObject.SocialSecurityStatement },
        { it as? SyncObject.SocialSecurityStatement }
    )

    class FiscalStatement<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.FiscalStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.FiscalStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.FiscalStatement) -> C?,
        override val getContentInFull: (item: SyncObject.FiscalStatement) -> C?
    ) : CopyFieldContentMapper<SummaryObject.FiscalStatement, SyncObject.FiscalStatement, C>(
        { it as? SummaryObject.FiscalStatement },
        { it as? SyncObject.FiscalStatement }
    )

    class EmailContent<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Email) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Email) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Email) -> C?,
        override val getContentInFull: (item: SyncObject.Email) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Email, SyncObject.Email, C>(
        { it as? SummaryObject.Email },
        { it as? SyncObject.Email }
    )

    class Phone<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Phone) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Phone) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Phone) -> C?,
        override val getContentInFull: (item: SyncObject.Phone) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Phone, SyncObject.Phone, C>(
        { it as? SummaryObject.Phone },
        { it as? SyncObject.Phone }
    )

    class PersonalWebsite<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.PersonalWebsite) -> Boolean,
        override val hasContentInFull: (item: SyncObject.PersonalWebsite) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.PersonalWebsite) -> C?,
        override val getContentInFull: (item: SyncObject.PersonalWebsite) -> C?
    ) : CopyFieldContentMapper<SummaryObject.PersonalWebsite, SyncObject.PersonalWebsite, C>(
        { it as? SummaryObject.PersonalWebsite },
        { it as? SyncObject.PersonalWebsite }
    )

    class Identity<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Identity) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Identity) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Identity) -> C?,
        override val getContentInFull: (item: SyncObject.Identity) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Identity, SyncObject.Identity, C>(
        { it as? SummaryObject.Identity },
        { it as? SyncObject.Identity }
    )

    class Company<C : Any>(
        override val hasContentInSummary: (item: SummaryObject.Company) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Company) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Company) -> C?,
        override val getContentInFull: (item: SyncObject.Company) -> C?
    ) : CopyFieldContentMapper<SummaryObject.Company, SyncObject.Company, C>(
        { it as? SummaryObject.Company },
        { it as? SyncObject.Company }
    )
}