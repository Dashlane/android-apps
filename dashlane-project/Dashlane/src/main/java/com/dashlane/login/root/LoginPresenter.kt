package com.dashlane.login.root

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dashlane.R
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorPresenter
import com.dashlane.login.pages.biometric.BiometricPresenter
import com.dashlane.login.pages.email.LoginEmailContract
import com.dashlane.login.pages.email.LoginEmailPresenter
import com.dashlane.login.pages.password.LoginPasswordContract
import com.dashlane.login.pages.password.LoginPasswordPresenter
import com.dashlane.login.pages.pin.PinErrorBottomSheet
import com.dashlane.login.pages.pin.PinLockPresenter
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.login.pages.sso.SsoLockPresenter
import com.dashlane.login.pages.token.LoginTokenPresenter
import com.dashlane.login.pages.totp.LoginTotpPresenter
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.login.toSecurityFeatures
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.ui.screens.settings.WarningRememberMasterPasswordDialog
import com.dashlane.ui.util.FinishingActivity
import com.dashlane.util.Toaster
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.getParcelableArrayCompat
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Deque
import java.util.LinkedList

@Suppress("LargeClass")
class LoginPresenter(
    private val viewModelProvider: ViewModelProvider,
    private val parentJob: Job,
    private val lockManager: LockManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory,
    private val allowSkipEmail: Boolean,
    private val loginLogger: LoginLogger,
    private val endOfLife: EndOfLife,
    private val toaster: Toaster,
    private val navigator: Navigator,
    private val warningRememberMasterPasswordDialog: WarningRememberMasterPasswordDialog,
    private val globalPreferencesManager: GlobalPreferencesManager
) : BasePresenter<LoginContract.DataProvider, LoginContract.LoginViewProxy>(),
    LoginContract.Presenter {

    override var showProgress: Boolean
        get() = pagesStateHelper.currentPresenter?.showProgress ?: false
        set(value) {
            pagesStateHelper.currentPresenter?.showProgress = value
        }

    var migrationToSsoMemberInfo: MigrationToSsoMemberInfo? = null

    private val pagesStateHelper = PagesStateHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val credentials = provider.currentUserInfo
            when {
                provider.isAlreadyLoggedIn() -> {
                    provider.lockSetting.isLoggedIn = true
                    showLockPage()
                }
                credentials == null -> showEmailPage()
                credentials.sso -> {
                    if (allowSkipEmail) {
                        skipEmailPage()
                    } else {
                        showEmailPage()
                    }
                }
                credentials.otp2 -> skipEmailPage()
                globalPreferencesManager.isUserLoggedOut -> {
                    if (allowSkipEmail) {
                        showPasswordPage(
                            RegisteredUserDevice.Local(
                                login = credentials.username,
                                securityFeatures = credentials.securitySettings.toSecurityFeatures(),
                                accessKey = credentials.accessKey
                            )
                        )
                    } else {
                        showEmailPage()
                    }
                }
                else -> {
                    showPrimaryFactorStep(
                        registeredUserDevice = RegisteredUserDevice.Local(
                            login = credentials.username,
                            securityFeatures = credentials.securitySettings.toSecurityFeatures(),
                            accessKey = credentials.accessKey
                        ),
                        authTicket = null
                    )
                }
            }
        } else {
            pagesStateHelper.readState(savedInstanceState)
            migrationToSsoMemberInfo =
                savedInstanceState.getParcelableCompat(STATE_MIGRATION_TO_SSO_MEMBER_INFO)
        }
    }

    override fun onStart() {
        pagesStateHelper.currentPresenter?.onStart()
    }

    fun onNewIntent() {
        pagesStateHelper.currentPresenter?.onNewIntent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        pagesStateHelper.writeState(outState)
        outState.putParcelable(STATE_MIGRATION_TO_SSO_MEMBER_INFO, migrationToSsoMemberInfo)
    }

    override fun onBackPressed(): Boolean {
        return if (pagesStateHelper.currentPresenter?.let { onBackPressed(it) } == true) {
            
            true
        } else {
            if (pagesStateHelper.hasPrevious) {
                
                pagesStateHelper.removedLastPage()
                true
            } else if (!provider.lockSetting.isLoggedIn) {
                FinishingActivity.finishApplication(activity!!)
                true
            } else if (provider.lockSetting.isLockCancelable) {
                cancelUnlock()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroy() {
        
    }

    @Suppress("UNCHECKED_CAST")
    private fun onBackPressed(presenter: LoginBaseContract.Presenter): Boolean {
        return presenter.onBackPressed()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pagesStateHelper.currentPresenter?.onActivityResult(requestCode, resultCode, data)
    }

    fun skipEmailPage() {
        showEmailPage()
        (pagesStateHelper.currentPresenter as? LoginEmailContract.Presenter)?.skipIfPrefilled()
    }

    fun showEmailPage() {
        val presenter = createEmailPresenter()
        pagesStateHelper.addedPage(presenter, null)
    }

    private fun createEmailPresenter(): LoginEmailPresenter {
        val dataProvider = provider.createEmailDataProvider()
        return LoginEmailPresenter(
            this,
            createChildCoroutineScope(),
            endOfLife,
            contactSsoAdministratorDialogFactory
        ).apply {
            setProvider(dataProvider)
        }
    }

    fun showTokenPage(
        emailSecondFactor: AuthenticationSecondFactor.EmailToken,
        replacePage: Boolean = false
    ) {
        val presenter = createTokenPresenter(emailSecondFactor)
        pagesStateHelper.addedPage(presenter, emailSecondFactor, replacePage)
        loginLogger.logAskAuthentication(LoginMode.MasterPassword(verification = VerificationMode.EMAIL_TOKEN))
    }

    private fun createTokenPresenter(emailSecondFactor: AuthenticationSecondFactor.EmailToken): LoginTokenPresenter {
        val dataProvider = provider.createTokenDataProvider(emailSecondFactor)
        return LoginTokenPresenter(this, createChildCoroutineScope(), loginLogger).apply {
            setProvider(dataProvider)
        }
    }

    fun showDashlaneAuthenticatorPage(
        secondFactor: AuthenticationSecondFactor,
        replacePage: Boolean = false
    ) {
        val presenter = createDashlaneAuthenticatorPresenter(secondFactor)
        pagesStateHelper.addedPage(presenter, secondFactor, replacePage)
        loginLogger.logAskAuthentication(LoginMode.MasterPassword(verification = VerificationMode.AUTHENTICATOR_APP))
    }

    fun showSecretTransferQRPage(email: String?) {
        view.transitionToCompose(email)
    }

    private fun createDashlaneAuthenticatorPresenter(secondFactor: AuthenticationSecondFactor): LoginDashlaneAuthenticatorPresenter {
        val dataProvider = provider.createAuthenticatorProvider(secondFactor)
        return LoginDashlaneAuthenticatorPresenter(
            this,
            createChildCoroutineScope(),
            viewModelProvider.getDeferredViewModel("dashlane_authenticator")
        ).apply {
            setProvider(dataProvider)
        }
    }

    fun showTotpPage(
        totpSecondFactor: AuthenticationSecondFactor.Totp,
        replacePage: Boolean = false
    ) {
        val presenter = createTotpPresenter(totpSecondFactor)
        pagesStateHelper.addedPage(presenter, totpSecondFactor, replacePage)
        val verification = if (provider.currentUserInfo?.otp2 == true) {
            VerificationMode.OTP2
        } else {
            VerificationMode.OTP1
        }
        loginLogger.logAskAuthentication(LoginMode.MasterPassword(verification = verification))
    }

    private fun createTotpPresenter(totpSecondFactor: AuthenticationSecondFactor.Totp): LoginTotpPresenter {
        val dataProvider = provider.createTotpDataProvider(totpSecondFactor)
        return LoginTotpPresenter(this, createChildCoroutineScope()).apply {
            setProvider(dataProvider)
        }
    }

    @Suppress("kotlin:S6313") 
    suspend fun onTotpSuccess(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        provider.initializeStoredSession(registeredUserDevice.login, registeredUserDevice.serverKey)
        showPrimaryFactorStep(registeredUserDevice, authTicket)
    }

    override fun onPrimaryFactorCancelOrLogout() {
        if (provider.lockSetting.isLockCancelable) {
            cancelUnlock()
        } else {
            navigator.logoutAndCallLoginScreen(context!!)
        }
    }

    private fun cancelUnlock(shouldFinish: Boolean = true) {
        lockManager.sendUnLock(UnlockEvent.Reason.Unknown(), false)
        if (!shouldFinish) return
        activity?.setResult(Activity.RESULT_CANCELED)
        activity?.finish()
    }

    override fun onPrimaryFactorTooManyAttempts() {
        logAskUsePrimaryFactor()
        cancelUnlockAndLogout()
    }

    override fun onBiometricNegativeClicked() {
        when (provider.currentUserInfo?.accountType) {
            UserAccountInfo.AccountType.InvisibleMasterPassword -> {
                switchLayoutToMainLockIfNeeded()
                showPinLockPage()
            }
            UserAccountInfo.AccountType.MasterPassword,
            null -> {
                switchLayoutToMainLockIfNeeded()
                logAskUsePrimaryFactor()
                showMainLock(false)
            }
        }
    }

    private fun switchLayoutToMainLockIfNeeded() {
        
        
        activity?.apply {
            if (provider.getLockType(this) == LockTypeManager.LOCK_TYPE_BIOMETRIC &&
                provider.lockSetting.shouldThemeAsDialog
            ) {
                window?.setBackgroundDrawable(ColorDrawable(getThemeAttrColor(android.R.attr.colorBackground)))
            }
        }
    }

    private fun cancelUnlockAndLogout() {
        provider.lockSetting.isLoggedIn = false
        cancelUnlock(false)
        val isAccountMasterPassword =
            provider.currentUserInfo?.accountType == UserAccountInfo.AccountType.MasterPassword

        if (isAccountMasterPassword) {
            navigator.logoutAndCallLoginScreen(context!!, allowSkipEmail = allowSkipEmail)
        } else {
            
            viewModelScope.launch {
                sessionManager.session?.let {
                    sessionManager.destroySession(
                        it,
                        true
                    )
                }
            }
            val bottomSheet = PinErrorBottomSheet(object : PinErrorBottomSheet.Actions {
                override fun goToLogin() {
                    navigator.logoutAndCallLoginScreen(context!!, allowSkipEmail = allowSkipEmail)
                }

                override fun goToSupport() {
                    activity?.startActivity(
                        HelpCenterLink.ARTICLE_CANNOT_LOGIN.newIntent(
                            context = context!!
                        )
                    )
                }
            })
            bottomSheet.show((activity as FragmentActivity).supportFragmentManager, "PIN_ERROR")
        }
    }

    fun showPrimaryFactorStep(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        if (provider.isAlreadyLoggedIn()) {
            showLockPage()
        } else {
            
            showPasswordPage(registeredUserDevice, authTicket)
        }
    }

    fun showPasswordPage(
        registeredUserDevice: RegisteredUserDevice,
        authTicket: String? = null,
        showForRemember: Boolean = false
    ) {
        val presenter = createPasswordPresenter(
            registeredUserDevice = registeredUserDevice,
            authTicket = authTicket,
            clearPreviousState = true,
            topicLock = if (showForRemember) context!!.getString(R.string.login_enter_mp_remember_title) else null, 
            allowBypass = showForRemember && provider.canDelayMasterPasswordUnlock()
        )
        pagesStateHelper.addedPage(presenter, registeredUserDevice)
        if ((registeredUserDevice as? RegisteredUserDevice.Local)?.isServerKeyRequired == false) {
            loginLogger.logAskAuthentication(LoginMode.MasterPassword(verification = VerificationMode.NONE))
        }
    }

    fun showLockPage() {
        if (provider.lockSetting.isPinSetter) {
            showPinLockPage()
        } else {
            if (provider.forceMasterPasswordUnlock(provider.lockSetting.unlockReason)) {
                showMainLock(true)
                return
            }
            when (provider.getLockType(context!!)) {
                LockTypeManager.LOCK_TYPE_PIN_CODE -> {
                    showPinLockPage()
                }
                LockTypeManager.LOCK_TYPE_BIOMETRIC -> {
                    showBiometricPage()
                }
                else -> showMainLock()
            }
        }
    }

    private fun createPasswordPresenter(
        registeredUserDevice: RegisteredUserDevice,
        authTicket: String?,
        clearPreviousState: Boolean = false,
        topicLock: String? = null,
        allowBypass: Boolean = false
    ): LoginPasswordPresenter {
        val dataProvider = provider.createPasswordDataProvider(
            registeredUserDevice,
            authTicket,
            migrationToSsoMemberInfo,
            topicLock = topicLock,
            allowBypass = allowBypass
        )
        val passwordValidationHolder =
            viewModelProvider.getDeferredViewModel<LoginPasswordContract.SuccessfulLogin>(
                VIEW_MODEL_PASSWORD_VALIDATION
            )
        if (clearPreviousState) {
            passwordValidationHolder.deferred = null
        }
        return LoginPasswordPresenter(
            rootPresenter = this,
            coroutineScope = createChildCoroutineScope(),
            passwordValidationHolder = passwordValidationHolder,
            lockManager = lockManager,
            loginLogger = loginLogger,
            toaster = toaster
        ).apply {
            setProvider(dataProvider)
        }
    }

    private fun createChildCoroutineScope() =
        CoroutineScope(Dispatchers.Main + SupervisorJob(parentJob))

    private fun showMainLock(showForRemember: Boolean = false) {
        val userInfo = provider.currentUserInfo ?: return

        if (userInfo.sso) {
            showSsoLockPage()
        } else if (userInfo.accountType == UserAccountInfo.AccountType.InvisibleMasterPassword) {
            showPinLockPage()
        } else {
            showPasswordPage(
                registeredUserDevice = RegisteredUserDevice.Local(
                    login = userInfo.username,
                    securityFeatures = userInfo.securitySettings.toSecurityFeatures(),
                    accessKey = userInfo.accessKey
                ),
                authTicket = null,
                showForRemember = showForRemember
            )
        }
    }

    private fun showPinLockPage() {
        val presenter = createPinLockPresenter()
        pagesStateHelper.addedPage(presenter, null)
        loginLogger.logAskAuthentication(LoginMode.Pin)
    }

    private fun createPinLockPresenter(): PinLockPresenter {
        val dataProvider = provider.createPinLockDataProvider()
        return PinLockPresenter(
            rootPresenter = this,
            coroutineScope = createChildCoroutineScope(),
            lockManager = lockManager,
            sso = provider.currentUserInfo?.sso == true,
            toaster = toaster,
            warningRememberMasterPasswordDialog = warningRememberMasterPasswordDialog
        ).apply {
            setProvider(dataProvider)
        }
    }

    private fun showBiometricPage() {
        val presenter = createBiometricPresenter()
        pagesStateHelper.addedPage(presenter, null)
        loginLogger.logAskAuthentication(LoginMode.Biometric)
    }

    private fun createBiometricPresenter(): BiometricPresenter {
        val dataProvider = provider.createBiometricDataProvider()
        return BiometricPresenter(
            rootPresenter = this,
            coroutineScope = createChildCoroutineScope(),
            lockManager = lockManager,
            userPreferencesManager = userPreferencesManager,
            sessionManager = sessionManager,
            sessionCredentialsSaver = sessionCredentialsSaver,
            loginLogger = loginLogger,
            toaster = toaster
        ).apply {
            setProvider(dataProvider)
        }
    }

    private fun showSsoLockPage() {
        val presenter = createSsoLockPresenter()
        pagesStateHelper.addedPage(presenter, null)
        loginLogger.logAskAuthentication(LoginMode.Sso)
    }

    private fun createSsoLockPresenter(): SsoLockContract.Presenter {
        val dataProvider = provider.createSsoLockDataProvider()

        return SsoLockPresenter(
            rootPresenter = this,
            coroutineScope = createChildCoroutineScope(),
            lockManager = lockManager,
            loginLogger = loginLogger,
            toaster = toaster
        ).apply { setProvider(dataProvider) }
    }

    fun showNextScreen(intent: Intent) {
        context?.startActivity(intent)
    }

    private fun updateCurrentPageView() {
        pagesStateHelper.currentPresenter?.toPage(hasActiveSession = sessionManager.session != null)
            ?.let { setCurrentPageView(it) }
    }

    private fun logAskUsePrimaryFactor() {
        val mode = when (pagesStateHelper.currentPresenter) {
            is BiometricPresenter -> Mode.BIOMETRIC
            is PinLockPresenter -> Mode.PIN
            
            is LoginPasswordPresenter, is SsoLockPresenter -> return
            else -> error("Unknown Presenter type")
        }

        val sso = provider.currentUserInfo?.sso ?: false

        if (sso) {
            loginLogger.logAskUseSso(mode)
        } else {
            loginLogger.logAskUseMasterPassword(mode)
        }
    }

    private inner class PagesStateHelper {

        val currentPresenter
            get() = pageStates.lastOrNull()?.first

        private val pageStates: Deque<Pair<LoginBaseContract.Presenter, Parcelable?>> =
            LinkedList<Pair<LoginBaseContract.Presenter, Parcelable?>>()

        val hasPrevious
            get() = pageStates.size > 1

        fun addedPage(
            presenter: LoginBaseContract.Presenter,
            state: Parcelable?,
            replacePage: Boolean = false
        ) {
            val previous = if (replacePage) {
                pageStates.removeLast().first.apply { coroutineContext.cancel() }
            } else {
                currentPresenter
            }
            pageStates.add(presenter to state)
            view.transition(previous, presenter)
            onCreatePresenter(presenter, null)
            updateCurrentPageView()
        }

        fun removedLastPage() {
            val from = pageStates.removeLast().first.apply { coroutineContext.cancel() }
            val lastPresenter = pageStates.last().first
            view.transition(from, lastPresenter)
            updateCurrentPageView()
        }

        fun writeState(bundle: Bundle) {
            pageStates.forEach { it.first.onSaveInstanceState(bundle) }
            val pageNames = pageStates.map { it.first.javaClass.name }
            val pageStates = pageStates.map { it.second }
            bundle.putStringArray(STATE_PAGE_NAMES, pageNames.toTypedArray())
            bundle.putParcelableArray(STATE_PAGE_STATES, pageStates.toTypedArray())
        }

        fun readState(bundle: Bundle) {
            val pageNames = bundle.getStringArray(STATE_PAGE_NAMES)!!
            val pageStates = bundle.getParcelableArrayCompat<Parcelable?>(STATE_PAGE_STATES)!!
            check(pageNames.size == pageStates.size) { "sizes don't match" }
            pageNames.zip(pageStates).forEach { (name, state) ->
                val presenter: LoginBaseContract.Presenter = when (name) {
                    LoginEmailPresenter::class.java.name -> createEmailPresenter()
                    LoginTokenPresenter::class.java.name -> createTokenPresenter(state as AuthenticationSecondFactor.EmailToken)
                    LoginTotpPresenter::class.java.name -> createTotpPresenter(state as AuthenticationSecondFactor.Totp)
                    LoginPasswordPresenter::class.java.name -> createPasswordPresenter(
                        state as RegisteredUserDevice,
                        null
                    )
                    BiometricPresenter::class.java.name -> createBiometricPresenter()
                    PinLockPresenter::class.java.name -> createPinLockPresenter()
                    SsoLockPresenter::class.java.name -> createSsoLockPresenter()
                    LoginDashlaneAuthenticatorPresenter::class.java.name -> createDashlaneAuthenticatorPresenter(
                        state as AuthenticationSecondFactor
                    )
                    else -> error("unknown presenter class $name")
                }
                this.pageStates.add(presenter to state)
                onCreatePresenter(presenter, bundle)
            }
            
            
            val lastPresenter = this.pageStates.lastOrNull()?.first ?: createEmailPresenter()
            view.transitionTo(lastPresenter)
        }

        @Suppress("UNCHECKED_CAST")
        private fun onCreatePresenter(presenter: LoginBaseContract.Presenter, bundle: Bundle?) {
            presenter.onCreate(bundle)
        }
    }

    companion object {
        private const val STATE_PAGE_NAMES = "login_pages"
        private const val STATE_PAGE_STATES = "login_page_states"
        private const val STATE_MIGRATION_TO_SSO_MEMBER_INFO = "login_migration_to_sso_member_info"

        private const val VIEW_MODEL_PASSWORD_VALIDATION = "password_validation"
    }
}

private fun LoginBaseContract.Presenter.toPage(hasActiveSession: Boolean): AnyPage? = when (this) {
    is LoginEmailPresenter -> AnyPage.LOGIN_EMAIL
    is LoginTokenPresenter -> AnyPage.LOGIN_TOKEN
    is LoginPasswordPresenter -> if (hasActiveSession) AnyPage.UNLOCK_MP else AnyPage.LOGIN_MASTER_PASSWORD
    is BiometricPresenter -> AnyPage.UNLOCK_BIOMETRIC
    is PinLockPresenter -> AnyPage.UNLOCK_PIN
    else -> null
}
