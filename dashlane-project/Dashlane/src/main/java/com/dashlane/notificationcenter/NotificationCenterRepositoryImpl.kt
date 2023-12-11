package com.dashlane.notificationcenter

import android.content.Context
import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.announcements.modules.trialupgraderecommendation.TrialUpgradeRecommendationModule
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.notification.badge.SharingInvitationRepositoryImpl
import com.dashlane.notificationcenter.promotions.IntroOfferActionItem
import com.dashlane.notificationcenter.view.ActionItem
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.notificationcenter.view.AlertActionItem
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.notificationcenter.view.SharingActionItemCollection
import com.dashlane.notificationcenter.view.SharingActionItemItemGroup
import com.dashlane.notificationcenter.view.SharingActionItemUserGroup
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.premium.offer.common.StoreOffersFormatter
import com.dashlane.premium.offer.common.StoreOffersManager
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.toPromotionType
import com.dashlane.security.SecurityHelper
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.tryOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@Suppress("LargeClass")
class NotificationCenterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesManager: UserPreferencesManager,
    private val lockManager: LockManager,
    private val inAppLoginManager: InAppLoginManager,
    private val mainDataAccessor: MainDataAccessor,
    private val breachLoader: BreachLoader,
    private val sharingLoader: SharingInvitationRepositoryImpl,
    private val sharingXmlConverter: DataIdentifierSharingXmlConverter,
    private val dataStorageProvider: DataStorageProvider,
    private val sessionManager: SessionManager,
    private val securityHelper: SecurityHelper,
    private val biometricAuthModule: BiometricAuthModule,
    private val biometricRecovery: BiometricRecovery,
    private val accountStatusRepository: AccountStatusRepository,
    private val darkWebMonitoringManager: DarkWebMonitoringManager,
    private val storeOffersManager: StoreOffersManager,
    private val storeOffersFormatter: StoreOffersFormatter,
    private val userAccountStorage: UserAccountStorage,
    @DefaultCoroutineDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher
) : NotificationCenterRepository {

    private val hasBiometric
        get() = biometricAuthModule.isHardwareSupported()

    private val lockType get() = lockManager.getLockType()

    private val actionItemTypes: Map<ActionItemSection, Set<ActionItemType>> by lazy { createActionItemTypes() }

    override fun isRead(item: NotificationItem) =
        userPreferencesManager.getBoolean("$PREFERENCE_READ${item.trackingKey}", false)

    override fun markDismissed(item: NotificationItem, dismissed: Boolean) {
        setDismiss(item, dismissed)
    }

    override fun markAsRead(item: NotificationItem) {
        setRead(item)
    }

    override suspend fun loadAll(): List<NotificationItem> = coroutineScope {
        actionItemTypes.map {
            async {
                load(
                    section = it.key,
                    limit = NotificationCenterPresenter.SECTION_CAPPING_THRESHOLD
                )
            }
        }.awaitAll().flatten()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override suspend fun load(section: ActionItemSection, limit: Int?): List<NotificationItem> {
        val types = actionItemTypes[section] ?: return listOf()
        return withContext(defaultCoroutineDispatcher) {
            types.map { type ->
                createActionItem(type, limit).filterNotNull().filterNotDismissed()
            }.flatten()
        }
    }

    override suspend fun hasAtLeastOneUnRead(): Boolean =
        loadAll().filterNotDismissedAndUnRead().isNotEmpty()

    override fun getOrInitCreationDate(item: NotificationItem): Instant {
        if (item is AlertActionItem) {
            return Instant.ofEpochMilli(item.breachWrapper.publicBreach.breachCreationDate * 1000L)
        }
        val creationDate = getFirstDisplayedDate(item.trackingKey)
        return if (creationDate < 0) {
            val currentTimeStamp = System.currentTimeMillis()
            setFirstDisplayedDate(item.trackingKey, currentTimeStamp)
            Instant.ofEpochMilli(currentTimeStamp)
        } else {
            Instant.ofEpochMilli(creationDate)
        }
    }

    private fun createActionItemTypes(): Map<ActionItemSection, Set<ActionItemType>> {
        val gettingStartedItemTypes = mutableSetOf(
            ActionItemType.AUTO_FILL,
            ActionItemType.PIN_CODE,
            ActionItemType.BIOMETRIC,
            ActionItemType.ZERO_PASSWORD,
            ActionItemType.ACCOUNT_RECOVERY
        )

        val whatIsNewItemTypes = mutableSetOf<ActionItemType>()
        whatIsNewItemTypes.add(ActionItemType.AUTHENTICATOR_ANNOUNCEMENT)

        val yourAccountItemTypes = setOf(
            ActionItemType.FREE_TRIAL_STARTED,
            ActionItemType.TRIAL_UPGRADE_RECOMMENDATION
        )

        return mapOf(
            ActionItemSection.BREACH_ALERT to setOf(ActionItemType.BREACH_ALERT),
            ActionItemSection.SHARING to setOf(ActionItemType.SHARING),
            ActionItemSection.PROMOTIONS to setOf(ActionItemType.INTRODUCTORY_OFFERS),
            ActionItemSection.GETTING_STARTED to gettingStartedItemTypes,
            ActionItemSection.WHATS_NEW to whatIsNewItemTypes,
            ActionItemSection.YOUR_ACCOUNT to yourAccountItemTypes
        )
    }

    private suspend fun createActionItem(type: ActionItemType, limit: Int?): List<NotificationItem?> {
        return when (type) {
            ActionItemType.AUTO_FILL -> listOf(createAutoFillItem())
            ActionItemType.BIOMETRIC, ActionItemType.PIN_CODE -> listOf(createSecureLockItem(type))
            ActionItemType.ZERO_PASSWORD -> listOf(createZeroPasswordItem())
            ActionItemType.BREACH_ALERT -> loadBreachItems(limit)
            ActionItemType.SHARING -> loadSharingItems()
            ActionItemType.ACCOUNT_RECOVERY -> listOf(createBiometricRecoveryItem())
            ActionItemType.FREE_TRIAL_STARTED -> listOf(createFreeTrialStartedItem())
            ActionItemType.TRIAL_UPGRADE_RECOMMENDATION -> listOf(
                createTrialUpgradeRecommendationItem()
            )
            ActionItemType.AUTHENTICATOR_ANNOUNCEMENT -> listOf(createAuthenticatorAnnouncementItem())
            ActionItemType.INTRODUCTORY_OFFERS -> createPromotionItems()
        }
    }

    private fun createZeroPasswordItem(): ActionItem? =
        if (meetConditionZeroPassword()) ActionItem.ZeroPasswordActionItem(this) else null

    private fun createAutoFillItem(): ActionItem? =
        if (meetConditionAutoFill()) {
            ActionItem.AutoFillActionItem(this)
        } else {
            null
        }

    private fun createBiometricRecoveryItem(): ActionItem? = if (meetConditionBiometricRecovery()) {
        ActionItem.BiometricRecoveryActionItem(
            this,
            lockType == LockTypeManager.LOCK_TYPE_BIOMETRIC
        )
    } else {
        null
    }

    private fun createSecureLockItem(type: ActionItemType): ActionItem? {
        val isDeviceSecured = securityHelper.isDeviceSecured()
        return when {
            
            meetConditionBiometric(type, hasBiometric, isDeviceSecured, lockType) ->
                ActionItem.BiometricActionItem(this)
            
            meetConditionPinCode(type, hasBiometric, isDeviceSecured, lockType) ->
                ActionItem.PinCodeActionItem(this)
            else -> null
        }
    }

    private fun createFreeTrialStartedItem(): ActionItem? =
        ActionItem.FreeTrialStartedActionItem(this)
            .takeIf { meetConditionFreeTrialStarted() }

    private suspend fun createTrialUpgradeRecommendationItem(): ActionItem? =
        if (meetConditionTrialUpgradeRecommendation()) {
            val offerType = getTrialUpgradeRecommendationType()
            ActionItem.TrialUpgradeRecommendationActionItem(this, offerType)
        } else {
            null
        }

    private suspend fun createPromotionItems(): List<NotificationItem> {
        val pendingOffers = loadStoreOffers().map { storeOffer ->
            val offerType = storeOffer.offerType
            val introOffers =
                listOf(storeOffer.monthly?.productDetails, storeOffer.yearly?.productDetails)
                    .filterIsInstance<ProductDetailsWrapper.IntroductoryOfferProduct>()

            introOffers.mapNotNull { productDetails ->
                productDetails.toPromotionType(offerType)?.let { offerData ->
                    IntroOfferActionItem(
                        actionItemsRepository = this,
                        introOfferType = offerData
                    )
                }
            }
        }.flatten()
        return pendingOffers
    }

    private suspend fun getTrialUpgradeRecommendationType(): OfferType =
        when (isDarkWebMonitoringSetup()) {
            true -> OfferType.PREMIUM
            else -> OfferType.ADVANCED
        }

    private suspend fun isDarkWebMonitoringSetup(): Boolean =
        darkWebMonitoringManager.getEmailsWithStatus()
            ?.any { it.status == DarkWebEmailStatus.STATUS_ACTIVE } ?: false

    private fun createAuthenticatorAnnouncementItem() =
        if (meetConditionAuthenticatorAnnouncement()) {
            ActionItem.AuthenticatorAnnouncementItem(this)
        } else {
            null
        }

    private fun meetConditionBiometric(
        type: ActionItemType,
        hasBiometric: Boolean,
        isDeviceSecured: Boolean,
        lockType: Int
    ): Boolean =
        type == ActionItemType.BIOMETRIC && hasBiometric && isDeviceSecured && lockType != LockTypeManager.LOCK_TYPE_BIOMETRIC

    private fun meetConditionPinCode(
        type: ActionItemType,
        hasBiometric: Boolean,
        isDeviceSecured: Boolean,
        lockType: Int
    ): Boolean =
        type == ActionItemType.PIN_CODE && !hasBiometric && isDeviceSecured && lockType != LockTypeManager.LOCK_TYPE_PIN_CODE

    private fun meetConditionAutoFill(): Boolean =
        !inAppLoginManager.isEnableForApp() && !DeveloperUtilities.isManaged(context)

    private fun meetConditionBiometricRecovery(): Boolean = securityHelper.isDeviceSecured() &&
        hasBiometric &&
        biometricRecovery.isFeatureAvailable() &&
        !biometricRecovery.isFeatureEnabled

    private fun meetConditionZeroPassword(): Boolean {
        val passwordCount = mainDataAccessor.getCredentialDataQuery()
            .queryAllPasswords()
            .filter { it.isNotSemanticallyNull() }
            .size
        return passwordCount == 0
    }

    private fun meetConditionFreeTrialStarted(): Boolean =
        sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }
            ?.isTrial
            ?: false

    private fun meetConditionTrialUpgradeRecommendation(): Boolean {
        val daysLeftForDisplay = TrialUpgradeRecommendationModule.REMAINING_TRIALS_DAYS
        return sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }
            ?.run { isTrial && remainingDays < daysLeftForDisplay + 1 }
            ?: false
    }

    private fun meetConditionAuthenticatorAnnouncement() =
        !userPreferencesManager.isAuthenticatorGetStartedDisplayed &&
            sessionManager.session?.username?.let { userAccountStorage[it]?.accountType is UserAccountInfo.AccountType.MasterPassword } ?: true

    private suspend fun loadBreachItems(limit: Int?): List<AlertActionItem> {
        val list = withContext(defaultCoroutineDispatcher) { breachLoader.getBreachesWrapper(limit) }
        return list.map { breachWrapper ->
            AlertActionItem(actionItemsRepository = this, breachWrapper = breachWrapper)
        }
    }

    private suspend fun loadSharingItems(): List<NotificationItem> {
        val list = sharingLoader.loadAllInvitations()
        return list.itemGroupInvitations.map { invite ->
            SharingActionItemItemGroup(
                actionItemsRepository = this,
                sharing = invite,
                xmlConverter = sharingXmlConverter,
                dataStorageProvider = dataStorageProvider,
                sessionManager = sessionManager
            ).apply {
                this.firstDisplayedDate = getOrInitCreationDate(this)
            }
        } + list.userGroupInvitations.map { invite ->
            SharingActionItemUserGroup(
                actionItemsRepository = this,
                sharing = invite,
                xmlConverter = sharingXmlConverter,
                dataStorageProvider = dataStorageProvider,
                sessionManager = sessionManager
            ).apply {
                this.firstDisplayedDate = getOrInitCreationDate(this)
            }
        } + list.collectionInvitations.map { invite ->
            SharingActionItemCollection(
                actionItemsRepository = this,
                sharing = invite,
                xmlConverter = sharingXmlConverter,
                dataStorageProvider = dataStorageProvider,
                sessionManager = sessionManager
            ).apply {
                this.firstDisplayedDate = getOrInitCreationDate(this)
            }
        }
    }

    private suspend fun loadStoreOffers(): List<FormattedStoreOffer> =
        tryOrNull {
            storeOffersManager.fetchProductsForCurrentUser().let {
                storeOffersFormatter.build(it)
            }
        } ?: listOf()

    private fun setRead(item: NotificationItem) =
        userPreferencesManager.putBoolean("$PREFERENCE_READ${item.trackingKey}", true)

    private fun getFirstDisplayedDate(trackingKey: String) =
        userPreferencesManager.getLong("$PREFERENCE_CREATION_DATE$trackingKey", -1)

    private fun setFirstDisplayedDate(trackingKey: String, value: Long) =
        userPreferencesManager.putLong("$PREFERENCE_CREATION_DATE$trackingKey", value)

    private fun isDismiss(item: NotificationItem) =
        userPreferencesManager.getBoolean("$PREFERENCE_DISMISS${item.trackingKey}", false)

    private fun setDismiss(item: NotificationItem, value: Boolean) =
        setDismissed(userPreferencesManager, item.trackingKey, value)

    private fun Iterable<NotificationItem>.filterNotDismissed() =
        filterTo(ArrayList()) { !isDismiss(it) }

    private fun Iterable<NotificationItem>.filterNotDismissedAndUnRead() =
        filterTo(ArrayList()) { !isDismiss(it) && !isRead(it) }

    companion object {
        private const val PREFERENCE_DISMISS = "action_item_dismiss_"
        private const val PREFERENCE_READ = "action_item_read_"
        private const val PREFERENCE_CREATION_DATE = "action_item_creation_date_"

        fun setDismissed(
            userPreferencesManager: UserPreferencesManager,
            key: String,
            value: Boolean
        ) {
            userPreferencesManager.putBoolean("$PREFERENCE_DISMISS$key", value)
        }
    }
}