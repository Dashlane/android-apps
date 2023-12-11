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
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.toFieldItemType
import javax.inject.Inject

class FollowUpNotificationLoggerImpl @Inject constructor(
    private val logRepository: LogRepository
) : FollowUpNotificationLogger {

    override fun showFollowUp(followUpType: FollowUpNotificationsTypes, copyField: CopyField?) {
        
        logRepository.queueEvent(
            FollowUpNotification(
                itemType = followUpType.toItemType(),
                action = FollowUpNotificationActions.TRIGGER
            )
        )
    }

    override fun dismissFollowUp(followUpType: FollowUpNotificationsTypes) {
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
        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = AnyPage.NOTIFICATION_FOLLOW_UP_NOTIFICATION_DISCOVERY
        )
    }

    override fun logDisplayDiscoveryReminder() {
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
}