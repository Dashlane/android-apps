package com.dashlane.autofill.api.followup

import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.FollowUpNotificationActions
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.CopyVaultItemFieldAnonymous
import com.dashlane.hermes.generated.events.user.CopyVaultItemField
import com.dashlane.hermes.generated.events.user.FollowUpNotification
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.toFieldItemType
import javax.inject.Inject

class FollowUpNotificationLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : FollowUpNotificationLogger {

    override fun showFollowUp(followUpType: FollowUpNotificationsTypes, copyField: CopyField?) {
        
        val actionSuffix = copyField?.toFollowUpLogActionSuffix() ?: FOLLOW_UP_NOTIFICATION_UL75_AUTOFILL_SUFFIX
        log(
            UsageLogCode75(
                action = FOLLOW_UP_NOTIFICATION_UL75_TRIGGER_ACTION_PREFIX + actionSuffix,
                type = FOLLOW_UP_NOTIFICATION_UL75_TYPE,
                subtype = FOLLOW_UP_NOTIFICATION_UL75_SUB_TYPE,
                subaction = followUpType.toFollowUpLogSubAction()
            )
        )
        logRepository.queueEvent(
            FollowUpNotification(
                itemType = followUpType.toItemType(),
                action = FollowUpNotificationActions.TRIGGER
            )
        )
    }

    override fun dismissFollowUp(followUpType: FollowUpNotificationsTypes) {
        log(
            UsageLogCode75(
                action = FOLLOW_UP_NOTIFICATION_UL75_DISMISS_ACTION,
                type = FOLLOW_UP_NOTIFICATION_UL75_TYPE,
                subtype = FOLLOW_UP_NOTIFICATION_UL75_SUB_TYPE,
                subaction = followUpType.toFollowUpLogSubAction()
            )
        )
        logRepository.queueEvent(
            FollowUpNotification(
                itemType = followUpType.toItemType(),
                action = FollowUpNotificationActions.DISMISS
            )
        )
    }

    override fun copyFieldFromFollowUp(
        followUpType: FollowUpNotificationsTypes,
        copiedField: CopyField,
        itemId: String,
        isProtected: Boolean,
        domain: String?
    ) {
        val fieldItemType = copiedField.toFieldItemType()
        log(
            UsageLogCode75(
                action = FOLLOW_UP_NOTIFICATION_UL75_COPY_ACTION_PREFIX + copiedField.toFollowUpLogActionSuffix(),
                type = FOLLOW_UP_NOTIFICATION_UL75_TYPE,
                subtype = FOLLOW_UP_NOTIFICATION_UL75_SUB_TYPE,
                subaction = followUpType.toFollowUpLogSubAction()
            )
        )
        logRepository.queueEvent(
            CopyVaultItemField(
                field = fieldItemType.first,
                itemId = ItemId(itemId),
                itemType = fieldItemType.second,
                isProtected = isProtected
            )
        )
        domain?.let {
            logRepository.queueEvent(
                CopyVaultItemFieldAnonymous(
                    field = fieldItemType.first,
                    itemType = fieldItemType.second,
                    domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
                )
            )
        }
        logRepository.queueEvent(
            FollowUpNotification(
                itemType = fieldItemType.second,
                action = FollowUpNotificationActions.COPY
            )
        )
    }

    override fun logDisplayDiscoveryIntroduction() {
        log(
            UsageLogCode75(
                type = FOLLOW_UP_NOTIFICATION_UL75_TYPE,
                subtype = FOLLOW_UP_NOTIFICATION_UL75_PROMPT_INTRODUCTION_SUB_TYPE,
                action = FOLLOW_UP_NOTIFICATION_UL75_VALIDATE_ACTION,
                origin = UsageLogCode75.Origin.ITEM_EDIT
            )
        )
        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = AnyPage.NOTIFICATION_FOLLOW_UP_NOTIFICATION_DISCOVERY
        )
    }

    override fun logDisplayDiscoveryReminder() {
        log(
            UsageLogCode75(
                type = FOLLOW_UP_NOTIFICATION_UL75_TYPE,
                subtype = FOLLOW_UP_NOTIFICATION_UL75_PROMPT_REMINDER_SUB_TYPE,
                action = FOLLOW_UP_NOTIFICATION_UL75_VALIDATE_ACTION,
                origin = UsageLogCode75.Origin.ITEM_EDIT
            )
        )
        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = AnyPage.NOTIFICATION_FOLLOW_UP_NOTIFICATION_REMINDER
        )
    }

    override fun logDeactivateFollowUpNotification() {
        logRepository.queueEvent(
            FollowUpNotification(
                action = FollowUpNotificationActions.DEACTIVATE_FEATURE
            )
        )
    }

    override fun logActivateFollowUpNotification() {
        logRepository.queueEvent(
            FollowUpNotification(
                action = FollowUpNotificationActions.ACTIVATE_FEATURE
            )
        )
    }

    private fun log(log: UsageLog) = bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)

    private fun FollowUpNotificationsTypes.toItemType(): ItemType {
        return when (this) {
            FollowUpNotificationsTypes.PASSWORDS -> ItemType.CREDENTIAL
            FollowUpNotificationsTypes.ADDRESS -> ItemType.ADDRESS
            FollowUpNotificationsTypes.BANK_ACCOUNT,
            FollowUpNotificationsTypes.BANK_ACCOUNT_US,
            FollowUpNotificationsTypes.BANK_ACCOUNT_GB,
            FollowUpNotificationsTypes.BANK_ACCOUNT_MX -> ItemType.BANK_STATEMENT
            FollowUpNotificationsTypes.DRIVERS_LICENSE -> ItemType.DRIVER_LICENCE
            FollowUpNotificationsTypes.ID_CARD -> ItemType.ID_CARD
            FollowUpNotificationsTypes.PASSPORT -> ItemType.PASSPORT
            FollowUpNotificationsTypes.PAYMENTS_CARD -> ItemType.CREDIT_CARD
            FollowUpNotificationsTypes.PAYPAL -> ItemType.PAYPAL
        }
    }

    private fun FollowUpNotificationsTypes.toFollowUpLogSubAction(): String {
        return when (this) {
            FollowUpNotificationsTypes.PASSWORDS -> "Passwords"
            FollowUpNotificationsTypes.ADDRESS -> "Address"
            FollowUpNotificationsTypes.BANK_ACCOUNT,
            FollowUpNotificationsTypes.BANK_ACCOUNT_US,
            FollowUpNotificationsTypes.BANK_ACCOUNT_GB,
            FollowUpNotificationsTypes.BANK_ACCOUNT_MX -> "BankAccount"
            FollowUpNotificationsTypes.DRIVERS_LICENSE -> "DriversLicense"
            FollowUpNotificationsTypes.ID_CARD -> "IDCard"
            FollowUpNotificationsTypes.PASSPORT -> "Passport"
            FollowUpNotificationsTypes.PAYMENTS_CARD -> "PaymentsCard"
            FollowUpNotificationsTypes.PAYPAL -> "Paypal"
        }
    }

    private fun CopyField.toFollowUpLogActionSuffix(): String? {
        return when (this) {
            CopyField.SecondaryLogin -> "SecondaryLogin"
            CopyField.PaymentsSecurityCode -> "SecurityCode"
            CopyField.BankAccountBank -> "Bank"
            CopyField.BankAccountBicSwift,
            CopyField.BankAccountRoutingNumber,
            CopyField.BankAccountSortCode -> "BicSwift"
            CopyField.BankAccountIban,
            CopyField.BankAccountAccountNumber,
            CopyField.BankAccountClabe -> "Iban"
            CopyField.Address -> "Address"
            CopyField.City -> "City"
            CopyField.ZipCode -> "ZipCode"
            CopyField.JustEmail -> "Email"
            CopyField.PhoneNumber -> "PhoneNumber"
            CopyField.PersonalWebsite -> "Website"
            CopyField.Password,
            CopyField.PayPalPassword -> "Password"
            CopyField.Login,
            CopyField.Email,
            CopyField.PayPalLogin -> "Login"
            CopyField.PaymentsNumber,
            CopyField.IdsNumber,
            CopyField.PassportNumber,
            CopyField.DriverLicenseNumber,
            CopyField.SocialSecurityNumber,
            CopyField.TaxNumber -> "Number"
            CopyField.PaymentsExpirationDate,
            CopyField.IdsExpirationDate,
            CopyField.PassportExpirationDate,
            CopyField.DriverLicenseExpirationDate -> "ExpirationDate"
            CopyField.IdsIssueDate,
            CopyField.PassportIssueDate,
            CopyField.DriverLicenseIssueDate -> "IssueDate"
            CopyField.OtpCode -> "OtpCode"
            else -> null
        }
    }

    companion object {
        private const val FOLLOW_UP_NOTIFICATION_UL75_TYPE = "followUpNotification"
        private const val FOLLOW_UP_NOTIFICATION_UL75_SUB_TYPE = "notification"
        private const val FOLLOW_UP_NOTIFICATION_UL75_PROMPT_REMINDER_SUB_TYPE = "swipeDownPrompt"
        private const val FOLLOW_UP_NOTIFICATION_UL75_PROMPT_INTRODUCTION_SUB_TYPE = "accessInfoPrompt"
        private const val FOLLOW_UP_NOTIFICATION_UL75_TRIGGER_ACTION_PREFIX = "trigger"
        private const val FOLLOW_UP_NOTIFICATION_UL75_COPY_ACTION_PREFIX = "copy"
        private const val FOLLOW_UP_NOTIFICATION_UL75_DISMISS_ACTION = "dismiss"
        private const val FOLLOW_UP_NOTIFICATION_UL75_VALIDATE_ACTION = "validate"
        private const val FOLLOW_UP_NOTIFICATION_UL75_AUTOFILL_SUFFIX = "fromAutofill"
    }
}