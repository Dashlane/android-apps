package com.dashlane.login.root

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dashlane.R
import com.dashlane.debug.DaDaDa
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginLoggerImpl
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job

@AndroidEntryPoint
class LoginLegacyFragment : Fragment() {

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
            lockSetting = LockSetting.buildFrom(extras)
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
            loginLogger = LoginLoggerImpl(logRepository),
            endOfLife = endOfLife,
            toaster = toaster,
            navigator = navigator,
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
