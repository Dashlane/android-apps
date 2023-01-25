@file:JvmName("DataIdentifierUtils")

package com.dashlane.vault.util

import android.content.Context
import androidx.annotation.CheckResult
import com.dashlane.R
import com.dashlane.attachment.AttachmentsParser
import com.dashlane.session.Session
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.CreditCardBank.Companion.US_NO_TYPE
import com.dashlane.vault.model.PAYPAL
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.utils.Country
import java.time.Instant



fun SummaryObject.hasAttachments(): Boolean = attachmentsCount() > 0



fun SummaryObject.attachmentsCount(): Int = AttachmentsParser().parse(attachments).size

@CheckResult
fun VaultItem<*>.copyWithDefaultValue(context: Context, session: Session?): VaultItem<*> {
    @Suppress("UNCHECKED_CAST")
    return when (this.syncObject) {
        is SyncObject.Authentifiant -> copyAuthentifiantWithDefaultValue(
            this as VaultItem<SyncObject.Authentifiant>,
            context,
            session
        )
        is SyncObject.Address -> copyAddressWithDefaultValue(
            this as VaultItem<SyncObject.Address>,
            context
        )
        is SyncObject.AuthCategory -> copyAuthCategoryWithDefaultValue(
            this as VaultItem<SyncObject.AuthCategory>,
            context
        )
        is SyncObject.BankStatement -> copyBankStatementWithDefaultValue(
            this as VaultItem<SyncObject.BankStatement>,
            context
        )
        is SyncObject.Company -> copyCompanyWithDefaultValue(
            this as VaultItem<SyncObject.Company>,
            context
        )
        is SyncObject.Email -> copyEmailWithDefaultValue(
            this as VaultItem<SyncObject.Email>,
            context
        )
        is SyncObject.PaymentCreditCard -> copyPaymentCreditCardWithDefaultValue(this as VaultItem<SyncObject.PaymentCreditCard>)
        is SyncObject.PaymentPaypal -> copyPaymentPaypalWithDefaultValue(this as VaultItem<SyncObject.PaymentPaypal>)
        is SyncObject.PersonalWebsite -> copyPersonalWebsiteWithDefaultValue(
            this as VaultItem<SyncObject.PersonalWebsite>,
            context
        )
        is SyncObject.Phone -> copyPhoneWithDefaultValue(
            this as VaultItem<SyncObject.Phone>,
            context
        )
        is SyncObject.SecureNote -> copySecureNoteWithDefaultValue(
            this as VaultItem<SyncObject.SecureNote>,
            context
        )
        is SyncObject.SecureNoteCategory -> copySecureNoteCategoryWithDefaultValue(
            this as VaultItem<SyncObject.SecureNoteCategory>,
            context
        )
        else -> this
    } as VaultItem<*>
}

@CheckResult
fun VaultItem<SyncObject.Authentifiant>.copyWithDefaultLogin(session: Session): VaultItem<SyncObject.Authentifiant> {
    return this.copySyncObject { email = session.userId }
}

@CheckResult
fun VaultItem<SyncObject.Authentifiant>.copyWithNewUrl(url: String): VaultItem<SyncObject.Authentifiant> {
    val now = Instant.now()
    return this.copySyncObject {
        this.url = url
        this.userSelectedUrl = url
        this.modificationDatetime = now
    }
}

@CheckResult
fun VaultItem<SyncObject.Authentifiant>.copyWithNewPassword(password: String): VaultItem<SyncObject.Authentifiant> {
    val now = Instant.now()
    return this.copySyncObject {
        this.password = SyncObfuscatedValue(password)
        this.modificationDatetime = now
    }
}

@CheckResult
private fun copyAuthentifiantWithDefaultValue(
    authentifiant: VaultItem<SyncObject.Authentifiant>,
    context: Context,
    session: Session?
): VaultItem<SyncObject.Authentifiant>? {
    if (!authentifiant.syncObject.title.isSemanticallyNull() && !authentifiant.syncObject.loginForUi.isSemanticallyNull()) return authentifiant

    var newAuthentifiant = authentifiant
    if (authentifiant.syncObject.title.isSemanticallyNull()) {
        newAuthentifiant = newAuthentifiant.copyWithUpdatedAuthentifiantTitle(context)
    }
    if (authentifiant.syncObject.loginForUi.isSemanticallyNull() && session != null) {
        newAuthentifiant = newAuthentifiant.copyWithDefaultLogin(session)
    }
    return newAuthentifiant
}

@CheckResult
private fun VaultItem<SyncObject.Authentifiant>.copyWithUpdatedAuthentifiantTitle(
    context: Context
): VaultItem<SyncObject.Authentifiant> {
    return when {
        this.syncObject.url.isNotSemanticallyNull() -> try {
            this.syncObject.url?.toUrlOrNull()?.root?.let {
                this.copySyncObject { title = SyncObject.Authentifiant.formatTitle(it) }
            } ?: this
        } catch (e: Exception) {
            this
        }
        this.syncObject.userSelectedUrl.isNotSemanticallyNull() -> try {
            this.syncObject.userSelectedUrl?.toUrlOrNull()?.root?.let {
                this.copySyncObject { title = SyncObject.Authentifiant.formatTitle(it) }
            } ?: this
        } catch (e: Exception) {
            this
        }
        else -> this.copySyncObject { title = context.getString(R.string.login) }
    }
}

private fun copyAddressWithDefaultValue(vaultItem: VaultItem<SyncObject.Address>, context: Context):
        VaultItem<SyncObject.Address> =
    vaultItem.takeUnless { it.syncObject.addressName.isSemanticallyNull() }
        ?: vaultItem.copySyncObject { addressName = context.getString(R.string.address) }

private fun copyAuthCategoryWithDefaultValue(
    vaultItem: VaultItem<SyncObject.AuthCategory>,
    context: Context
):
        VaultItem<SyncObject.AuthCategory> {
    return if (vaultItem.syncObject.categoryName.isSemanticallyNull()) {
        vaultItem.copySyncObject {
            categoryName = context.getString(R.string.unspecified_category)
        }
    } else {
        vaultItem
    }
}

private fun copyBankStatementWithDefaultValue(
    bankStatement: VaultItem<SyncObject.BankStatement>,
    context: Context
):
        VaultItem<SyncObject.BankStatement> {
    if (!requireBankStatementCopy(bankStatement)) return bankStatement

    val name = if (bankStatement.syncObject.bankAccountName.isNullOrBlank() ||
        bankStatement.syncObject.bankAccountName.isSemanticallyNull()
    ) {
        context.getString(R.string.bank_statement)
    } else {
        bankStatement.syncObject.bankAccountName
    }
    val bank = bankStatement.syncObject.bankAccountBank ?: CreditCardBank(US_NO_TYPE).bankDescriptor
    val newLocalFormat =
        bankStatement.syncObject.localeFormat ?: Country.UnitedStates

    return bankStatement
        .copySyncObject {
            bankAccountName = name
            bankAccountBank = bank
            localeFormat = newLocalFormat
        }
}

private fun requireBankStatementCopy(vaultItem: VaultItem<SyncObject.BankStatement>) =
    vaultItem.syncObject.bankAccountName.isNullOrBlank() ||
            vaultItem.syncObject.bankAccountName.isSemanticallyNull() ||
            vaultItem.syncObject.bankAccountBank == null ||
            vaultItem.syncObject.localeFormat == null

private fun copyCompanyWithDefaultValue(vaultItem: VaultItem<SyncObject.Company>, context: Context):
        VaultItem<SyncObject.Company> {
    return if (vaultItem.syncObject.name.isSemanticallyNull()) {
        vaultItem.copySyncObject { name = context.getString(R.string.company) }
    } else {
        vaultItem
    }
}

private fun copyEmailWithDefaultValue(
    vaultItem: VaultItem<SyncObject.Email>,
    context: Context
): VaultItem<SyncObject.Email> {
    return if (vaultItem.syncObject.emailName.isSemanticallyNull()) {
        vaultItem.copySyncObject({ emailName = context.getString(R.string.email) })
    } else {
        vaultItem
    }
}

private fun copyPaymentCreditCardWithDefaultValue(creditCard: VaultItem<SyncObject.PaymentCreditCard>):
        VaultItem<SyncObject.PaymentCreditCard> {
    if (creditCard.syncObject.bank != null && creditCard.syncObject.localeFormat != null
    )
        return creditCard

    val mBank = if (creditCard.syncObject.bank == null) {
        CreditCardBank(US_NO_TYPE).bankDescriptor
    } else {
        creditCard.syncObject.bank
    }

    val newLocalFormat =
        creditCard.syncObject.localeFormat ?: Country.UnitedStates
    return creditCard
        .copySyncObject {
            localeFormat = newLocalFormat
            bank = mBank
        }
}

private fun copyPaymentPaypalWithDefaultValue(vaultItem: VaultItem<SyncObject.PaymentPaypal>):
        VaultItem<SyncObject.PaymentPaypal> {
    return if (vaultItem.syncObject.name.isSemanticallyNull()) {
        vaultItem.copySyncObject { name = SyncObject.PaymentPaypal.PAYPAL }
    } else {
        vaultItem
    }
}

private fun copyPersonalWebsiteWithDefaultValue(
    vaultItem: VaultItem<SyncObject.PersonalWebsite>,
    context: Context
):
        VaultItem<SyncObject.PersonalWebsite> {
    if (vaultItem.syncObject.name.isNotSemanticallyNull()) {
        return vaultItem
    }

    val newName = vaultItem.syncObject.website?.toUrlOrNull()?.host
    return if (newName.isNullOrBlank()) {
        vaultItem.copySyncObject { name = context.getString(R.string.website) }
    } else {
        vaultItem.copySyncObject { name = newName }
    }
}

private fun copyPhoneWithDefaultValue(
    vaultItem: VaultItem<SyncObject.Phone>,
    context: Context
): VaultItem<SyncObject.Phone> {
    return if (vaultItem.syncObject.phoneName.isSemanticallyNull()) {
        vaultItem.copySyncObject { phoneName = context.getString(R.string.phone) }
    } else {
        vaultItem
    }
}

private fun copySecureNoteWithDefaultValue(
    vaultItem: VaultItem<SyncObject.SecureNote>,
    context: Context
):
        VaultItem<SyncObject.SecureNote> {
    
    if (!vaultItem.syncObject.title.isSemanticallyNull() && !vaultItem.syncObject.content.isSemanticallyNull())
        return vaultItem

    val mTitle = if (vaultItem.syncObject.title.isSemanticallyNull()) {
        context.getString(R.string.securenote_name)
    } else {
        vaultItem.syncObject.title
    }

    val mContent = if (vaultItem.syncObject.content.isSemanticallyNull()) {
        ""
    } else {
        vaultItem.syncObject.content
    }

    return vaultItem.copySyncObject {
        title = mTitle
        content = mContent
    }
}

private fun copySecureNoteCategoryWithDefaultValue(
    vaultItem: VaultItem<SyncObject.SecureNoteCategory>,
    context: Context
): VaultItem<SyncObject.SecureNoteCategory> {
    return if (vaultItem.syncObject.categoryName.isSemanticallyNull()) {
        vaultItem.copySyncObject { categoryName = context.getString(R.string.unspecified_category) }
    } else {
        vaultItem
    }
}