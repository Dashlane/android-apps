package com.dashlane.login.lock

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.accessibility.DashlaneAccessibilityService
import com.dashlane.autofill.accessibility.alwayson.AlwaysOnEventHandler
import com.dashlane.events.AppEvents
import com.dashlane.events.getLastEvent
import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockSelfChecker
import com.dashlane.lock.LockWatcher
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.LockTimeManager.Companion.DEFAULT_AUTO_LOCK_GRACE_PERIOD
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.summary.SummaryObject
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class LockManager @Inject constructor(
    lockNavigationHelper: LockNavigationHelper,
    lockMessageHelper: LockMessageHelper,
    private val lockTypeManager: LockTypeManager,
    private val lockTimeManager: LockTimeManager,
    private val lockWatcher: LockWatcherImpl,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val lockValidator: LockValidator,
    private val lockSelfChecker: LockSelfCheckerImpl,
    private val appEvents: AppEvents
) :
    AutofillAnalyzerDef.ILockManager,
    LockHelper,
    LockTypeManager by lockTypeManager,
    LockNavigationHelper by lockNavigationHelper,
    LockMessageHelper by lockMessageHelper,
    LockTimeManager by lockTimeManager,
    LockWatcher by lockWatcher,
    LockSelfChecker by lockSelfChecker {

    override var isLocked = true
    override var hasEnteredMP = false

    override val isInAppLoginLocked: Boolean
        get() = isInAppLoginLocked(lockTimeout)

    init {
        lockWatcher.setLockHelper(this)
    }

    fun onUserChanged() {
        hasEnteredMP = false
        refreshSavedDuration()
    }

    fun setLockOnExit(enable: Boolean): Boolean {
        return userPreferencesManager.putBoolean(ConstantsPrefs.LOCK_ON_EXIT, enable)
    }

    fun setItemUnlockableByPinOrFingerprint(enable: Boolean) {
        userPreferencesManager.putBoolean(
            ConstantsPrefs.UNLOCK_ITEMS_WITH_PIN_OR_FP,
            enable
        )
    }

    override fun isLockedOrLogout(): Boolean {
        return isLocked || sessionManager.session == null
    }

    override fun startAutoLockGracePeriod(duration: Duration) {
        lockTimeManager.startAutoLockGracePeriod(duration, this)
    }

    override fun startAutoLockGracePeriod() {
        startAutoLockGracePeriod(DEFAULT_AUTO_LOCK_GRACE_PERIOD)
    }

    fun unlock(pass: LockPass): Boolean {
        return if (lockValidator.check(pass)) {
            unlockAndMarkTimestamp()
            if (pass is LockPass.PasswordPass) {
                
                userPreferencesManager.credentialsSaveDate = Instant.now()
            }
            true
        } else {
            false
        }
    }

    private fun unlockAndMarkTimestamp() {
        isLocked = false
        setLastActionTimestampToNow()
        resetAttemptsToUnlockCount()
        setUnlockTimestampToNow()
    }

    override fun lock() {
        lockWithoutEvents()
        sendLockEvent()
    }

    fun lockWithoutEvents() {
        isLocked = true
    }

    fun sendUnLock(reason: UnlockEvent.Reason, success: Boolean) {
        if (success && reason is UnlockEvent.Reason.AccessFromExternalComponent) {
            val eventHandler = DashlaneAccessibilityService.eventHandler
            if (eventHandler is AlwaysOnEventHandler) {
                val itemUid = reason.itemUid
                if (itemUid != null) {
                    eventHandler.alwaysOnUiManager.onItemPicked(itemUid)
                }
            }
            return
        }
        val unlockEvent = UnlockEvent(success, reason)
        appEvents.post(unlockEvent)
        sendUnlockEvent(unlockEvent)
    }

    fun hasRecentUnlock(itemUid: String): Boolean {
        val lastUnlock = lastUnlock ?: return false

        if (elapsedDuration(lastUnlock).abs() < Duration.ofSeconds(4)) {
            
            
            
            return true
        }

        val unlockEvent = appEvents.getLastEvent<UnlockEvent>() ?: return false
        val unlockEventReason = unlockEvent.reason
        return if (unlockEventReason is UnlockEvent.Reason.OpenItem) {
            itemUid == unlockEventReason.itemUid
        } else {
            false
        }
    }

    fun needUnlock(item: SummaryObject): Boolean {
        return isMasterPasswordRequiredForItem(item) || item is SummaryObject.SecureNote && item.secured ?: false
    }

    private fun isMasterPasswordRequiredForItem(item: SummaryObject): Boolean {
        val itemSecuredByLock = item is SummaryObject.SocialSecurityStatement
        val accessAllowanceExpired = !shouldAuthoriseForView()
        return itemSecuredByLock && accessAllowanceExpired
    }

    fun resetAttemptsToUnlockCount() {
        setAttemptsToUnlockCount(0)
    }

    private fun setAttemptsToUnlockCount(count: Int) {
        userPreferencesManager.pinCodeTryCount = count
    }

    fun onAppInBackground() {
        setLastActionTimestampToNow()
        onUserLeft()
    }

    private fun onUserLeft() {
        if (isLockOnExit() && !isInAutoLockGracePeriod()) {
            lockWithoutEvents()
        }
    }

    fun isLockOnExit(): Boolean {
        initLockOnExitWithTeamspace()
        return userPreferencesManager.getBoolean(
            ConstantsPrefs.LOCK_ON_EXIT,
            LockTimeManager.LOCK_ON_EXIT_DEFAULT_VALUE
        )
    }

    fun addFailUnlockAttempt() {
        setAttemptsToUnlockCount(getFailUnlockAttemptCount() + 1)
    }

    fun getFailUnlockAttemptCount(): Int =
        max(0, userPreferencesManager.pinCodeTryCount)

    fun hasFailedUnlockTooManyTimes(): Boolean = getFailUnlockAttemptCount() >= 3

    fun checkForInactivityLock() {
        val lockTimeout = lockTimeout ?: return 

        if (lockTimeout.isNegative) {
            
            return
        }

        if (sessionManager.session == null) {
            
            return
        }

        if (!isLocked) {
            
            val lastAction = lastAction
            
            val last = lastUnlock?.takeIf { lastAction == null || lastAction < it } ?: lastAction

            if (last == null || elapsedDuration(last).abs() > lockTimeout) {
                
                lock()
            }
        }
    }

    private fun initLockOnExitWithTeamspace() {
        val teamspaceAccessor = teamSpaceAccessorProvider.get() ?: return
        if (teamspaceAccessor.isLockOnExitEnabled) {
            
            setLockOnExit(true)
        }
    }

    private fun isInAppLoginLocked(lockTimeout: Duration?): Boolean {
        if (isLocked) return true 
        
        lockTimeout ?: return false
        if (lockTimeout.isNegative) return false
        val lastUnlock = lastUnlock ?: return true 
        return elapsedDuration(lastUnlock).abs() > lockTimeout
    }
}