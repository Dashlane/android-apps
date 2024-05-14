package com.dashlane.login

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StyleRes
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dashlane.R
import com.dashlane.debug.DaDaDa
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockSetting.Companion.buildFrom
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation
import com.dashlane.login.pages.totp.u2f.NfcServiceDetectorImpl
import com.dashlane.login.root.LoginDataProvider
import com.dashlane.login.root.LoginPresenter
import com.dashlane.login.root.LoginViewProxy
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.disableAutoFill
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.ui.screens.settings.WarningRememberMasterPasswordDialog
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job

@AndroidEntryPoint
class LoginActivity : DashlaneActivity() {

    @Inject
    lateinit var nfcServiceProvider: NfcServiceDetectorImpl

    @Inject
    @TrackingId
    lateinit var trackingId: String

    private var currentThemeResId = 0

    private val extras: Bundle?
        get() = intent?.extras

    companion object {
        internal const val LOCK_SETTING = "lock_setting"
        internal const val CURRENT_THEME_RES_ID = "current_theme_res_id"
        const val ALLOW_SKIP_EMAIL = "allow_skip_email"
        const val SYNC_ERROR = "sync_error"
    }

    override var requireUserUnlock: Boolean
        get() = false
        set(requireUserUnlock) {
            super.requireUserUnlock = requireUserUnlock
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        var lockSetting: LockSetting? = null
        if (savedInstanceState != null) {
            lockSetting = savedInstanceState.getParcelable(LOCK_SETTING)
        }
        if (lockSetting == null) {
            lockSetting = buildFrom(extras)
        }

        currentThemeResId =
            savedInstanceState?.getInt(CURRENT_THEME_RES_ID) ?: getAppropriateTheme(lockSetting)
        setTheme(currentThemeResId)
        super.onCreate(savedInstanceState)
        this.disableAutoFill()
        setContentView(R.layout.activity_login)
        window.applyScreenshotAllowedFlag(screenshotPolicy)
        lifecycle.addObserver(nfcServiceProvider)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        
        if (NfcAdapter.ACTION_TECH_DISCOVERED != intent.action) {
            setIntent(intent) 
        }
        nfcServiceProvider.onNewIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_THEME_RES_ID, currentThemeResId)
    }

    @StyleRes
    internal fun getAppropriateTheme(lockSetting: LockSetting): Int {
        val themeResId: Int = when {
            lockSetting.shouldThemeAsDialog -> R.style.Theme_Dashlane_Login_Dialog
            else -> R.style.Theme_Dashlane_Login_TranslucentWindow
        }
        return themeResId
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        
        val loginHostFragment =
            supportFragmentManager.fragments.firstOrNull()?.childFragmentManager?.fragments?.firstOrNull() as? LoginHostFragment
        loginHostFragment?.onActivityResult(requestCode, resultCode, data)
    }
}

@AndroidEntryPoint
class LoginHostFragment : Fragment() {

    @Inject
    lateinit var dataProvider: LoginDataProvider

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionCredentialsSaver: SessionCredentialsSaver

    @Inject
    lateinit var contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var endOfLife: EndOfLife

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var warningRememberMasterPasswordDialog: WarningRememberMasterPasswordDialog

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var daDaDa: DaDaDa

    lateinit var loginPresenter: LoginPresenter

    private val extras: Bundle?
        get() = requireActivity().intent?.extras

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var lockSetting: LockSetting? = null
        if (savedInstanceState != null) {
            lockSetting = savedInstanceState.getParcelable(LoginActivity.LOCK_SETTING)
        }
        if (lockSetting == null) {
            lockSetting = buildFrom(extras)
        }
        dataProvider.lockSetting = lockSetting

        val allowSkipEmail =
            requireActivity().intent.getBooleanExtra(LoginActivity.ALLOW_SKIP_EMAIL, false)
        loginPresenter = LoginPresenter(
            viewModelProvider = ViewModelProvider(this),
            parentJob = (requireActivity() as DashlaneActivity).coroutineContext[Job]!!,
            lockManager = lockManager,
            userPreferencesManager = userPreferencesManager,
            sessionManager = sessionManager,
            sessionCredentialsSaver = sessionCredentialsSaver,
            contactSsoAdministratorDialogFactory = contactSsoAdministratorDialogFactory,
            allowSkipEmail = allowSkipEmail,
            loginLogger = LoginLoggerImpl(logRepository, lockSetting.unlockReason),
            endOfLife = endOfLife,
            toaster = toaster,
            navigator = navigator,
            warningRememberMasterPasswordDialog = warningRememberMasterPasswordDialog,
            globalPreferencesManager = globalPreferencesManager,
            daDaDa = daDaDa
        )
        loginPresenter.setProvider(dataProvider)

        activity?.onBackPressedDispatcher?.let {
            it.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!loginPresenter.onBackPressed()) it.onBackPressed()
                    }
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewProxy = LoginViewProxy(view)
        loginPresenter.apply {
            setView(viewProxy)
            onCreate(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        loginPresenter.onSaveInstanceState(outState)
        outState.putParcelable(LoginActivity.LOCK_SETTING, dataProvider.lockSetting)
    }

    override fun onStart() {
        super.onStart()
        loginPresenter.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginPresenter.onActivityResult(requestCode, resultCode, data)
    }
}

@AndroidEntryPoint
class LoginComposeFragment : Fragment() {

    @Inject
    lateinit var secretTransferAnalytics: SecretTransferAnalytics

    val args: LoginComposeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val email: String? = args.email
        val startDestination: String = args.startDestination ?: LoginSecretTransferNavigation.qrCodeDestination

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    LoginSecretTransferNavigation(
                        secretTransferAnalytics = secretTransferAnalytics,
                        startDestination = startDestination,
                        email = email,
                        onSuccess = {
                            activity?.run {
                                secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SUCCESS)
                                startActivity(LoginIntents.createProgressActivityIntent(this))
                                finish()
                            }
                        },
                        onCancel = {
                            this@LoginComposeFragment.findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}
