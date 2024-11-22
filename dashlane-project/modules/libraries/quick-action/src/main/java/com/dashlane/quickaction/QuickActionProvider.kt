package com.dashlane.quickaction

import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.navigation.Navigator
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.ui.action.Action
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.utils.Country
import javax.inject.Inject

@Suppress("LargeClass")
class QuickActionProvider @Inject constructor(
    private val vaultItemCopyService: VaultItemCopyService,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val navigator: Navigator,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val frozenStateManager: FrozenStateManager,
) {

    private val isAccountFrozen: Boolean
        get() = frozenStateManager.isAccountFrozen

    fun hasQuickActions(summaryObject: SummaryObject): Boolean =
        buildQuickActions(summaryObject)
            .any()

    fun getQuickActions(summaryObject: SummaryObject): List<Action> {
        return buildQuickActions(summaryObject)
            .sortedBy { it.first }
            .map { it.second }
            .toList()
    }

    private fun buildQuickActions(summaryObject: SummaryObject): Sequence<Pair<Int, Action>> {
        return when (summaryObject) {
            is SummaryObject.Address -> getAddressQuickActions(summaryObject)
            is SummaryObject.Authentifiant -> getAuthentifiantQuickActions(summaryObject)
            is SummaryObject.BankStatement -> getBankStatementQuickActions(summaryObject)
            is SummaryObject.Company -> getCompanyQuickActions(summaryObject)
            is SummaryObject.DriverLicence -> getDriverLicenceQuickActions(summaryObject)
            is SummaryObject.Email -> getEmailQuickActions(summaryObject)
            is SummaryObject.FiscalStatement -> getFiscalStatementQuickActions(summaryObject)
            is SummaryObject.IdCard -> getIdCardQuickActions(summaryObject)
            is SummaryObject.Identity -> getIdentityQuickActions(summaryObject)
            is SummaryObject.Passkey -> getPasskeyQuickActions(summaryObject)
            is SummaryObject.Passport -> getPassportQuickActions(summaryObject)
            is SummaryObject.PaymentCreditCard -> getPaymentCreditCardQuickActions(summaryObject)
            is SummaryObject.PersonalWebsite -> getPersonalWebsiteQuickActions(summaryObject)
            is SummaryObject.Phone -> getPhoneQuickActions(summaryObject)
            is SummaryObject.SecureNote -> getSecureNoteQuickActions(summaryObject)
            is SummaryObject.SocialSecurityStatement -> getSocialSecurityStatementQuickActions(summaryObject)
            is SummaryObject.Secret -> getSecretQuickActions(summaryObject)
            else -> emptySequence()
        }
    }

    private fun getSecretQuickActions(summaryObject: SummaryObject.Secret): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.SecretValue,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.SecretId,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getSocialSecurityStatementQuickActions(summaryObject: SummaryObject.SocialSecurityStatement): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.SocialSecurityNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopyIdentity.createActionIfIdentityExist(
                vaultItemCopyService = vaultItemCopyService,
                summaryObject
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getSecureNoteQuickActions(summaryObject: SummaryObject.SecureNote): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionShare.createActionIfShareAvailable(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(0 to it) }

            QuickActionOpenAttachment.createAttachmentsAction(
                summaryObject,
                userFeaturesChecker = userFeaturesChecker,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen,
                hasCollections = false 
            )?.let { yield(1 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(2 to it) }
        }
    }

    private fun getPhoneQuickActions(summaryObject: SummaryObject.Phone): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.PhoneNumber,
                vaultItemCopyService,
                sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicyDataProvider,
                navigator,
                isAccountFrozen
            )?.let { yield(1 to it) }

            QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
                ?.let { yield(2 to it) }
        }
    }

    private fun getPersonalWebsiteQuickActions(summaryObject: SummaryObject.PersonalWebsite): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.PersonalWebsite,
                vaultItemCopyService,
                sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicyDataProvider,
                navigator,
                isAccountFrozen
            )?.let { yield(1 to it) }

            QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
                ?.let { yield(2 to it) }
        }
    }

    private fun getPassportQuickActions(summaryObject: SummaryObject.Passport): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.PassportNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopyIdentity.createActionIfIdentityExist(vaultItemCopyService, summaryObject)
                ?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(summaryObject, sharingPolicyDataProvider, navigator)
                ?.let { yield(3 to it) }
        }
    }

    private fun getIdentityQuickActions(summaryObject: SummaryObject.Identity): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.FirstName,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.LastName,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.MiddleName,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(2 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.IdentityLogin,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(3 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(4 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(5 to it) }
        }
    }

    private fun getPaymentCreditCardQuickActions(summaryObject: SummaryObject.PaymentCreditCard): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                CopyField.PaymentsNumber,
                vaultItemCopyService,
                sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.PaymentsSecurityCode,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getPasskeyQuickActions(summaryObject: SummaryObject.Passkey): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.PasskeyDisplayName,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionOpenWebsite(
                url = summaryObject.urlForGoToWebsite ?: "",
                linkedServices = null,
                navigator = navigator,
            ).let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getIdCardQuickActions(summaryObject: SummaryObject.IdCard): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.IdsNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopyIdentity.createActionIfIdentityExist(
                vaultItemCopyService = vaultItemCopyService,
                summaryObject
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getFiscalStatementQuickActions(summaryObject: SummaryObject.FiscalStatement): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.TaxNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.TaxOnlineNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getEmailQuickActions(summaryObject: SummaryObject.Email): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.JustEmail,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(1 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(2 to it) }
        }
    }

    private fun getDriverLicenceQuickActions(summaryObject: SummaryObject.DriverLicence): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.DriverLicenseNumber,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopyIdentity.createActionIfIdentityExist(
                vaultItemCopyService = vaultItemCopyService,
                summaryObject
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getCompanyQuickActions(summaryObject: SummaryObject.Company): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.CompanyName,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.CompanyTitle,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(2 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(3 to it) }
        }
    }

    private fun getAddressQuickActions(summaryObject: SummaryObject.Address): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.Address,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.City,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.ZipCode,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(2 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(3 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(4 to it) }
        }
    }

    private fun getAuthentifiantQuickActions(summaryObject: SummaryObject.Authentifiant): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.Email,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.Login,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(1 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.SecondaryLogin,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(2 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.Password,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(3 to it) }

            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.OtpCode,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(4 to it) }

            QuickActionOpenWebsite(
                url = summaryObject.urlForGoToWebsite ?: "",
                linkedServices = summaryObject.linkedServices,
                navigator = navigator,
            ).let { yield(5 to it) }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(6 to it) }

            QuickActionShare.createActionIfShareAvailable(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(7 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let { yield(8 to it) }
        }
    }

    @Suppress("LongMethod")
    private fun getBankStatementQuickActions(
        summaryObject: SummaryObject.BankStatement,
    ): Sequence<Pair<Int, Action>> {
        return sequence {
            QuickActionCopy.createActionIfFieldExist(
                summaryObject,
                copyField = CopyField.BankAccountBank,
                vaultItemCopyService = vaultItemCopyService,
                sharingPolicyDataProvider = sharingPolicyDataProvider
            )?.let { yield(0 to it) }

            when (summaryObject.bankAccountCountry) {
                Country.Mexico -> {
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountBicSwift,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(1 to it) }

                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountClabe,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(2 to it) }
                }
                Country.UnitedKingdom -> {
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountSortCode,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(1 to it) }

                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountAccountNumber,

                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(2 to it) }
                }
                Country.UnitedStates -> {
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountRoutingNumber,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(1 to it) }

                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountAccountNumber,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(2 to it) }
                }
                else -> {
                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountBicSwift,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(1 to it) }

                    QuickActionCopy.createActionIfFieldExist(
                        summaryObject,
                        copyField = CopyField.BankAccountIban,
                        vaultItemCopyService = vaultItemCopyService,
                        sharingPolicyDataProvider = sharingPolicyDataProvider
                    )?.let { yield(2 to it) }
                }
            }

            QuickActionEdit.createActionIfCanEdit(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator,
                isAccountFrozen = isAccountFrozen
            )?.let { yield(3 to it) }

            QuickActionDelete.createActionIfCanDelete(
                summaryObject,
                sharingPolicy = sharingPolicyDataProvider,
                navigator = navigator
            )?.let {
                yield(4 to it)
            }
        }
    }
}
