package com.dashlane.util.clipboard.vault

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

internal sealed class CopyFieldContentMapper<S : SummaryObject, F : SyncObject>(
    private val summaryObjectSafeCast: (SummaryObject) -> S?,
    private val syncObjectSafeCast: (SyncObject) -> F?
) {
    abstract val hasContentInSummary: (item: S) -> Boolean
    abstract val hasContentInFull: (item: F) -> Boolean
    abstract val getContentInSummary: (item: S) -> CopyContent?
    abstract val getContentInFull: (item: F) -> CopyContent?

    fun hasContent(summaryObject: SummaryObject): Boolean {
        val typedContent = summaryObjectSafeCast(summaryObject) ?: return false

        return hasContentInSummary(typedContent)
    }

    fun hasContent(syncObject: SyncObject): Boolean {
        val typedContent = syncObjectSafeCast(syncObject) ?: return false

        return hasContentInFull(typedContent)
    }

    fun getContent(summaryObject: SummaryObject): CopyContent? {
        val typedContent = summaryObjectSafeCast(summaryObject) ?: return null

        return getContentInSummary(typedContent)
    }

    fun getContent(syncObject: SyncObject): CopyContent? {
        val typedContent = syncObjectSafeCast(syncObject) ?: return null

        return getContentInFull(typedContent)
    }

    class ContentOnlyInSyncObjectException : Exception()

    class Authentifiant(
        override val hasContentInSummary: (item: SummaryObject.Authentifiant) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Authentifiant) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Authentifiant) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Authentifiant) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Authentifiant, SyncObject.Authentifiant>(
        { it as? SummaryObject.Authentifiant },
        { it as? SyncObject.Authentifiant }
    )

    class PaymentCreditCard(
        override val hasContentInSummary: (item: SummaryObject.PaymentCreditCard) -> Boolean,
        override val hasContentInFull: (item: SyncObject.PaymentCreditCard) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.PaymentCreditCard) -> CopyContent,
        override val getContentInFull: (item: SyncObject.PaymentCreditCard) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.PaymentCreditCard, SyncObject.PaymentCreditCard>(
        { it as? SummaryObject.PaymentCreditCard },
        { it as? SyncObject.PaymentCreditCard }
    )

    class BankStatement(
        override val hasContentInSummary: (item: SummaryObject.BankStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.BankStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.BankStatement) -> CopyContent,
        override val getContentInFull: (item: SyncObject.BankStatement) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.BankStatement, SyncObject.BankStatement>(
        { it as? SummaryObject.BankStatement },
        { it as? SyncObject.BankStatement }
    )

    class Address(
        override val hasContentInSummary: (item: SummaryObject.Address) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Address) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Address) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Address) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Address, SyncObject.Address>(
        { it as? SummaryObject.Address },
        { it as? SyncObject.Address }
    )

    class IdCard(
        override val hasContentInSummary: (item: SummaryObject.IdCard) -> Boolean,
        override val hasContentInFull: (item: SyncObject.IdCard) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.IdCard) -> CopyContent,
        override val getContentInFull: (item: SyncObject.IdCard) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.IdCard, SyncObject.IdCard>(
        { it as? SummaryObject.IdCard },
        { it as? SyncObject.IdCard }
    )

    class Passport(
        override val hasContentInSummary: (item: SummaryObject.Passport) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Passport) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Passport) -> CopyContent?,
        override val getContentInFull: (item: SyncObject.Passport) -> CopyContent?
    ) : CopyFieldContentMapper<SummaryObject.Passport, SyncObject.Passport>(
        { it as? SummaryObject.Passport },
        { it as? SyncObject.Passport }
    )

    class DriverLicence(
        override val hasContentInSummary: (item: SummaryObject.DriverLicence) -> Boolean,
        override val hasContentInFull: (item: SyncObject.DriverLicence) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.DriverLicence) -> CopyContent,
        override val getContentInFull: (item: SyncObject.DriverLicence) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.DriverLicence, SyncObject.DriverLicence>(
        { it as? SummaryObject.DriverLicence },
        { it as? SyncObject.DriverLicence }
    )

    class SocialSecurityStatement(
        override val hasContentInSummary: (item: SummaryObject.SocialSecurityStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.SocialSecurityStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.SocialSecurityStatement) -> CopyContent,
        override val getContentInFull: (item: SyncObject.SocialSecurityStatement) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.SocialSecurityStatement, SyncObject.SocialSecurityStatement>(
        { it as? SummaryObject.SocialSecurityStatement },
        { it as? SyncObject.SocialSecurityStatement }
    )

    class FiscalStatement(
        override val hasContentInSummary: (item: SummaryObject.FiscalStatement) -> Boolean,
        override val hasContentInFull: (item: SyncObject.FiscalStatement) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.FiscalStatement) -> CopyContent,
        override val getContentInFull: (item: SyncObject.FiscalStatement) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.FiscalStatement, SyncObject.FiscalStatement>(
        { it as? SummaryObject.FiscalStatement },
        { it as? SyncObject.FiscalStatement }
    )

    class EmailContent(
        override val hasContentInSummary: (item: SummaryObject.Email) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Email) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Email) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Email) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Email, SyncObject.Email>(
        { it as? SummaryObject.Email },
        { it as? SyncObject.Email }
    )

    class Phone(
        override val hasContentInSummary: (item: SummaryObject.Phone) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Phone) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Phone) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Phone) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Phone, SyncObject.Phone>(
        { it as? SummaryObject.Phone },
        { it as? SyncObject.Phone }
    )

    class PersonalWebsite(
        override val hasContentInSummary: (item: SummaryObject.PersonalWebsite) -> Boolean,
        override val hasContentInFull: (item: SyncObject.PersonalWebsite) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.PersonalWebsite) -> CopyContent,
        override val getContentInFull: (item: SyncObject.PersonalWebsite) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.PersonalWebsite, SyncObject.PersonalWebsite>(
        { it as? SummaryObject.PersonalWebsite },
        { it as? SyncObject.PersonalWebsite }
    )

    class Identity(
        override val hasContentInSummary: (item: SummaryObject.Identity) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Identity) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Identity) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Identity) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Identity, SyncObject.Identity>(
        { it as? SummaryObject.Identity },
        { it as? SyncObject.Identity }
    )

    class Company(
        override val hasContentInSummary: (item: SummaryObject.Company) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Company) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Company) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Company) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Company, SyncObject.Company>(
        { it as? SummaryObject.Company },
        { it as? SyncObject.Company }
    )

    class Passkey(
        override val hasContentInSummary: (item: SummaryObject.Passkey) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Passkey) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Passkey) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Passkey) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Passkey, SyncObject.Passkey>(
        { it as? SummaryObject.Passkey },
        { it as? SyncObject.Passkey }
    )

    class Secret(
        override val hasContentInSummary: (item: SummaryObject.Secret) -> Boolean,
        override val hasContentInFull: (item: SyncObject.Secret) -> Boolean,
        override val getContentInSummary: (item: SummaryObject.Secret) -> CopyContent,
        override val getContentInFull: (item: SyncObject.Secret) -> CopyContent
    ) : CopyFieldContentMapper<SummaryObject.Secret, SyncObject.Secret>(
        { it as? SummaryObject.Secret },
        { it as? SyncObject.Secret }
    )
}