package com.dashlane.util.clipboard.vault

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.activityLog
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.shouldCaptureActivityLog
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class VaultItemCopyLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) {
    @Suppress("LongMethod")
    fun sendLog(copyField: CopyField, summaryObject: SummaryObject) {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return
        if (!summaryObject.shouldCaptureActivityLog(teamSpaceAccessor)) return

        val (logType, properties) = when {
            summaryObject is SummaryObject.Authentifiant -> {
                ActivityLog.LogType.USER_COPIED_CREDENTIAL_FIELD to when (copyField) {
                    CopyField.Password -> {
                        Properties(
                            credentialDomain = summaryObject.url,
                            credentialLogin = summaryObject.email ?: summaryObject.login,
                            field = Properties.Field.PASSWORD,
                        )
                    }
                    CopyField.OtpCode -> {
                        Properties(
                            credentialDomain = summaryObject.url,
                            credentialLogin = summaryObject.email ?: summaryObject.login,
                            field = Properties.Field.OTP,
                        )
                    }
                    else -> return
                }
            }
            summaryObject is SummaryObject.PaymentCreditCard -> {
                ActivityLog.LogType.USER_COPIED_CREDIT_CARD_FIELD to when (copyField) {
                    CopyField.PaymentsNumber -> {
                        Properties(name = summaryObject.name, field = Properties.Field.NUMBER)
                    }
                    CopyField.PaymentsSecurityCode -> {
                        Properties(name = summaryObject.name, field = Properties.Field.CVV)
                    }
                    else -> return
                }
            }
            summaryObject is SummaryObject.BankStatement -> {
                ActivityLog.LogType.USER_COPIED_BANK_ACCOUNT_FIELD to when (copyField) {
                    CopyField.BankAccountBicSwift -> {
                        Properties(name = summaryObject.bankAccountName, field = Properties.Field.SWIFT)
                    }
                    CopyField.BankAccountIban -> {
                        Properties(name = summaryObject.bankAccountName, field = Properties.Field.IBAN)
                    }
                    CopyField.BankAccountSortCode -> {
                        Properties(name = summaryObject.bankAccountName, field = Properties.Field.SORT_CODE)
                    }
                    CopyField.BankAccountRoutingNumber -> {
                        Properties(name = summaryObject.bankAccountName, field = Properties.Field.ROUTING_NUMBER)
                    }
                    CopyField.BankAccountAccountNumber -> {
                        Properties(name = summaryObject.bankAccountName, field = Properties.Field.ACCOUNT_NUMBER)
                    }
                    else -> return
                }
            }
            copyField == CopyField.SecretValue && summaryObject is SummaryObject.Secret -> {
                ActivityLog.LogType.USER_COPIED_SECRET_FIELD to Properties(name = summaryObject.title)
            }
            else -> return
        }
        logRepository.queueEvent(activityLog(logType = logType, properties = properties))
    }
}