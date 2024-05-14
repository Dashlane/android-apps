package com.dashlane.item.subview.quickaction

import com.dashlane.item.subview.Action
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country
import javax.inject.Inject

@Suppress("LargeClass")
class QuickActionProvider @Inject constructor(
    private val vaultItemCopyService: VaultItemCopyService,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val navigator: Navigator,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator
) {

    @Suppress("LongMethod")
    fun getQuickActions(summaryObject: SummaryObject, itemListContext: ItemListContext): List<Action> {
        return when (summaryObject.syncObjectType) {
            SyncObjectType.ADDRESS -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.Address,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.City,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.ZipCode,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.AUTHENTIFIANT -> {
                return listOfNotNull(
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        CopyField.Email,
                        itemListContext,
                        vaultItemCopyService,
                        sharingPolicyDataProvider
                    ),
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        CopyField.Login,
                        itemListContext,
                        vaultItemCopyService,
                        sharingPolicyDataProvider
                    ),
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        CopyField.SecondaryLogin,
                        itemListContext,
                        vaultItemCopyService,
                        sharingPolicyDataProvider
                    ),
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        CopyField.Password,
                        itemListContext,
                        vaultItemCopyService,
                        sharingPolicyDataProvider
                    ),
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        CopyField.OtpCode,
                        itemListContext,
                        vaultItemCopyService,
                        sharingPolicyDataProvider
                    ),
                    QuickActionOpenWebsite(
                        (summaryObject as SummaryObject.Authentifiant).urlForGoToWebsite ?: "",
                        summaryObject.linkedServices
                    ),
                    QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                    QuickActionShare.createActionIfShareAvailable(
                        summaryObject,
                        sharingPolicyDataProvider,
                        teamspaceRestrictionNotificator
                    ),
                    QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
                )
            }
            SyncObjectType.BANK_STATEMENT -> getBankStatementQuickAction(
                summaryObject as SummaryObject.BankStatement,
                itemListContext,
                vaultItemCopyService,
                sharingPolicyDataProvider
            )
            SyncObjectType.DRIVER_LICENCE -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.DriverLicenseNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopyIdentity.createActionIfIdentityExist(vaultItemCopyService, summaryObject),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.FISCAL_STATEMENT -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.TaxNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.TaxOnlineNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.ID_CARD -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.IdsNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopyIdentity.createActionIfIdentityExist(vaultItemCopyService, summaryObject),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.PASSPORT -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PassportNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopyIdentity.createActionIfIdentityExist(vaultItemCopyService, summaryObject),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.PAYMENT_CREDIT_CARD -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PaymentsNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PaymentsSecurityCode,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.PAYMENT_PAYPAL -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PayPalLogin,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PayPalPassword,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.SECURE_NOTE -> listOfNotNull(
                QuickActionShare.createActionIfShareAvailable(
                    summaryObject,
                    sharingPolicyDataProvider,
                    teamspaceRestrictionNotificator
                ),
                QuickActionOpenAttachment.createAttachmentsAction(summaryObject, userFeaturesChecker),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.IDENTITY -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.FirstName,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.LastName,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.MiddleName,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.IdentityLogin,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.COMPANY -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.CompanyName,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.CompanyTitle,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.EMAIL -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.JustEmail,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.PERSONAL_WEBSITE -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PersonalWebsite,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.PHONE -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PhoneNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )
            SyncObjectType.SOCIAL_SECURITY_STATEMENT -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.SocialSecurityNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopyIdentity.createActionIfIdentityExist(vaultItemCopyService, summaryObject),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )

            SyncObjectType.PASSKEY -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.PasskeyDisplayName,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionOpenWebsite(
                    (summaryObject as SummaryObject.Passkey).urlForGoToWebsite ?: "",
                    null
                ),
                QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
                QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
            )

            else -> emptyList()
        }
    }

    @Suppress("LongMethod")
    private fun getBankStatementQuickAction(
        summaryObject: SummaryObject.BankStatement,
        itemListContext: ItemListContext,
        vaultItemCopyService: VaultItemCopyService,
        sharingPolicyDataProvider: SharingPolicyDataProvider
    ): List<Action> {
        return listOfNotNull(
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.BankAccountBank,
                itemListContext,
                vaultItemCopyService,
                sharingPolicyDataProvider
            )
        ) + when (summaryObject.bankAccountCountry) {
            Country.Mexico -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountBicSwift,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountClabe,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                )
            )
            Country.UnitedKingdom -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountSortCode,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountAccountNumber,

                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                )
            )
            Country.UnitedStates -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountRoutingNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountAccountNumber,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                )
            )
            else -> listOfNotNull(
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountBicSwift,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                ),
                QuickActionCopy.createActionIfFieldExist(
                    summaryObject,
                    CopyField.BankAccountIban,
                    itemListContext,
                    vaultItemCopyService,
                    sharingPolicyDataProvider
                )
            )
        } + listOfNotNull(
            QuickActionEdit.createActionIfCanEdit(summaryObject, sharingPolicyDataProvider, navigator),
            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicyDataProvider,
                navigator
            )
        )
    }
}
