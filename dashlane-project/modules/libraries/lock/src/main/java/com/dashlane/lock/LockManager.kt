package com.dashlane.lock

import com.dashlane.lock.LockTimeManager.Companion.DEFAULT_AUTO_LOCK_GRACE_PERIOD
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.user.Username
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
    private val lockTypeManager: LockTypeManager,
    private val lockTimeManager: LockTimeManager,
    private val lockWatcher: LockWatcherImpl,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val lockValidator: LockValidator,
    private val lockSelfChecker: LockSelfCheckerImpl,
) :
    LockHelper,
    LockTypeManager by lockTypeManager,
    LockNavigationHelper by lockNavigationHelper,
    LockTimeManager by lockTimeManager,
    LockWatcher by lockWatcher,
    LockSelfChecker by lockSelfChecker {

    override var isLocked = true
    override var hasEnteredMP = false

    init {
        lockWatcher.setLockHelper(this)
    }

    fun onUserChanged(username: Username) {
        hasEnteredMP = false
        refreshSavedDuration(username)
    }

    fun setLockOnExit(enable: Boolean): Boolean {
        return preferencesManager[sessionManager.session?.username].putBoolean(ConstantsPrefs.LOCK_ON_EXIT, enable)
    }

    override fun isLockedOrLogout(): Boolean {
        return isLocked || sessionManager.session == null
    }

    override fun isInAppLoginLocked(): Boolean {
        if (isLocked) return true 
        if (lockTimeout.isNegative) return false
        val lastUnlock = lastUnlock ?: return true 
        return elapsedDuration(lastUnlock).abs() > lockTimeout
    }

    override fun startAutoLockGracePeriod(duration: Duration) {
        lockTimeManager.startAutoLockGracePeriod(duration, this::lockWithoutEvents)
    }

    override fun startAutoLockGracePeriod() {
        startAutoLockGracePeriod(DEFAULT_AUTO_LOCK_GRACE_PERIOD)
    }

    fun unlock(session: Session, pass: LockPass): Boolean {
        return if (lockValidator.check(session, pass)) {
            unlockAndMarkTimestamp()
            if (pass is LockPass.PasswordPass) {
                
                preferencesManager[session.username].credentialsSaveDate = Instant.now()
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

    override suspend fun lock() {
        isLocked = true
        sendLockEvent()
    }

    fun lockWithoutEvents() {
        isLocked = true
    }

    fun hasRecentUnlock(): Boolean {
        val lastUnlock = lastUnlock ?: return false
        return elapsedDuration(lastUnlock).abs() < Duration.ofSeconds(4)
    }

    fun needUnlock(item: SummaryObject): Boolean {
        return isMasterPasswordRequiredForItem(item) ||
            item is SummaryObject.SecureNote && item.secured ?: false ||
            item is SummaryObject.Secret && item.secured ?: false
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
        preferencesManager[sessionManager.session?.username].pinCodeTryCount = count
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
        return preferencesManager[sessionManager.session?.username].getBoolean(
            ConstantsPrefs.LOCK_ON_EXIT,
            LockTimeManager.LOCK_ON_EXIT_DEFAULT_VALUE
        )
    }

    fun addFailUnlockAttempt() {
        setAttemptsToUnlockCount(getFailUnlockAttemptCount() + 1)
    }

    fun getFailUnlockAttemptCount(): Int {
        return max(0, preferencesManager[sessionManager.session?.username].pinCodeTryCount)
    }

    fun hasFailedUnlockTooManyTimes(): Boolean = getFailUnlockAttemptCount() >= 3

    suspend fun checkForInactivityLock() {
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
}