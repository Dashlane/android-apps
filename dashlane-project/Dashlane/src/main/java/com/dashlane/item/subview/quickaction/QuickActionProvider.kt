package com.dashlane.item.subview.quickaction

import com.dashlane.item.subview.Action
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country



@Suppress("LongMethod")
fun SummaryObject.getQuickActions(
    vaultContentService: VaultItemFieldContentService,
    itemListContext: ItemListContext
): List<Action> {
    return when (this.syncObjectType) {
        SyncObjectType.ADDRESS -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.Address,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.City,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.ZipCode,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.AUTHENTIFIANT -> {
            return listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    this,
                    CopyField.Email,
                    vaultContentService,
                    itemListContext
                ),
                QuickActionCopy.createActionIfFieldExist(
                    this,
                    CopyField.Login,
                    vaultContentService,
                    itemListContext
                ),
                QuickActionCopy.createActionIfFieldExist(
                    this,
                    CopyField.SecondaryLogin,
                    vaultContentService,
                    itemListContext
                ),
                QuickActionCopy.createActionIfFieldExist(
                    this,
                    CopyField.Password,
                    vaultContentService,
                    itemListContext
                ),
                QuickActionCopy.createActionIfFieldExist(
                    this,
                    CopyField.OtpCode,
                    vaultContentService,
                    itemListContext
                ),
                QuickActionOpenWebsite(
                    (this as SummaryObject.Authentifiant).urlForGoToWebsite ?: "",
                    linkedServices
                ),
                QuickActionShare.createActionIfShareAvailable(this),
                QuickActionDelete.createActionIfCanDelete(this)
            )
        }
        SyncObjectType.BANK_STATEMENT -> getBankStatementQuickAction(
            this as SummaryObject.BankStatement,
            vaultContentService,
            itemListContext
        )
        SyncObjectType.DRIVER_LICENCE -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.DriverLicenseNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopyIdentity.createActionIfIdentityExist(this),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.FISCAL_STATEMENT -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.TaxNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.TaxOnlineNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.ID_CARD -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.IdsNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopyIdentity.createActionIfIdentityExist(this),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.PASSPORT -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PassportNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopyIdentity.createActionIfIdentityExist(this),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.PAYMENT_CREDIT_CARD -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PaymentsNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PaymentsSecurityCode,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.PAYMENT_PAYPAL -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PayPalLogin,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PayPalPassword,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.SECURE_NOTE -> listOfNotNull(
            QuickActionShare.createActionIfShareAvailable(this),
            QuickActionOpenAttachment.createAttachmentsAction(this),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.IDENTITY -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.FirstName,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.LastName,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.MiddleName,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.IdentityLogin,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.COMPANY -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.CompanyName,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.CompanyTitle,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.EMAIL -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.JustEmail,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.PERSONAL_WEBSITE -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PersonalWebsite,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.PHONE -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.PhoneNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        SyncObjectType.SOCIAL_SECURITY_STATEMENT -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                this,
                CopyField.SocialSecurityNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopyIdentity.createActionIfIdentityExist(this),
            QuickActionDelete.createActionIfCanDelete(this)
        )
        else -> emptyList()
    }
}



@Suppress("LongMethod")
internal fun getBankStatementQuickAction(
    summaryObject: SummaryObject.BankStatement,
    vaultContentService: VaultItemFieldContentService,
    itemListContext: ItemListContext
): List<Action> {
    return listOfNotNull(
        QuickActionCopy.createActionIfFieldExist(
            summaryObject,
            CopyField.BankAccountBank,
            vaultContentService,
            itemListContext
        )
    ) + when (summaryObject.bankAccountCountry) {
        Country.Mexico -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountBicSwift,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountClabe,
                vaultContentService,
                itemListContext
            )
        )
        Country.UnitedKingdom -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountSortCode,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountAccountNumber,
                vaultContentService,
                itemListContext
            )
        )
        Country.UnitedStates -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountRoutingNumber,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountAccountNumber,
                vaultContentService,
                itemListContext
            )
        )
        else -> listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountBicSwift,
                vaultContentService,
                itemListContext
            ),
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountIban,
                vaultContentService,
                itemListContext
            )
        )
    } + listOfNotNull(QuickActionDelete.createActionIfCanDelete(summaryObject))
}
