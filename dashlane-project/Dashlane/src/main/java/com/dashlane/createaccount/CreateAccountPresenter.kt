package com.dashlane.createaccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.createaccount.pages.choosepassword.CreateAccountChoosePasswordPresenter
import com.dashlane.createaccount.pages.choosepassword.CreateAccountChoosePasswordViewProxy
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordPresenter
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordViewProxy
import com.dashlane.createaccount.pages.email.CreateAccountEmailPresenter
import com.dashlane.createaccount.pages.email.CreateAccountEmailViewProxy
import com.dashlane.createaccount.pages.settings.CreateAccountSettingsContract
import com.dashlane.createaccount.pages.settings.CreateAccountSettingsPresenter
import com.dashlane.createaccount.pages.settings.CreateAccountSettingsViewProxy
import com.dashlane.createaccount.pages.tos.AgreedTosEvent
import com.dashlane.createaccount.pages.tos.CreateAccountTosBottomSheetDialogFragment
import com.dashlane.createaccount.passwordless.MplessAccountCreationActivity
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.endoflife.EndOfLife
import com.dashlane.util.coroutines.DeferredViewModel
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAccountPresenter(
    private val coroutineScope: CoroutineScope,
    private val preFilledEmail: String?,
    private val skipEmailIfPrefilled: Boolean,
    private val endOfLife: EndOfLife,
    private val contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory
) : BasePresenter<CreateAccountContract.DataProvider, CreateAccountContract.ViewProxy>(),
    CreateAccountContract.Presenter {

    private var attemptedAccountCreation: Boolean = false

    override var showProgress: Boolean
        get() = view.showProgress
        set(value) {
            view.showProgress = value
        }

    var password: ObfuscatedByteArray?
        get() = pagesStateHelper.password
        set(value) {
            pagesStateHelper.password = value
        }

    val rootView: ConstraintLayout
        get() = view.root

    private val pagesStateHelper = PagesStateHelper()

    private val fragmentActivity
        get() = activity as FragmentActivity

    private lateinit var accountCreationHolder: DeferredViewModel<Unit>

    override fun onNextClicked() {
        pagesStateHelper.currentPresenter.onNextClicked()
        attemptedAccountCreation = true
    }

    override fun onMplessSetupClicked() {
        activity?.let {
            it.startActivity(
                Intent(it, MplessAccountCreationActivity::class.java).apply {
                    putExtra(MplessAccountCreationActivity.EXTRA_USER_LOGIN, pagesStateHelper.user)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showEmailPage(preFilledEmail, skipEmailIfPrefilled)
        } else {
            pagesStateHelper.readState(savedInstanceState)
        }
    }

    override fun onStart() {
        accountCreationHolder = ViewModelProvider(fragmentActivity)
            .getDeferredViewModel(
                "CreateAccountActivity_CreateAccount"
            )

        listenForTosEvents()

        
        accountCreationHolder.deferred?.let {
            createAccount(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        pagesStateHelper.writeState(outState)
    }

    override fun onBackPressed(): Boolean {
        return if (onBackPressed(pagesStateHelper.currentPresenter)) {
            
            true
        } else {
            val hasPrevious = pagesStateHelper.hasPrevious
            if (hasPrevious) {
                
                view.navigatePrevious()
                pagesStateHelper.removedLastPage()
            }
            hasPrevious
        }
    }

    override fun onDestroy() {
        pagesStateHelper.pageStates.forEach {
            it.onDestroy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pagesStateHelper.currentPresenter.onActivityResult(requestCode, resultCode, data)
    }

    private fun onBackPressed(presenter: CreateAccountBaseContract.Presenter): Boolean {
        return presenter.onBackPressed()
    }

    override fun updateSettings(biometric: Boolean, resetMp: Boolean) {
        pagesStateHelper.biometricAuthentication = biometric
        pagesStateHelper.resetMp = resetMp
    }

    private fun showEmailPage(preFilledEmail: String?, skipEmailIfPrefilled: Boolean) {
        val presenter = createEmailPresenter(preFilledEmail)
        pagesStateHelper.addedPage(presenter)
        if (skipEmailIfPrefilled && !attemptedAccountCreation) {
            onNextClicked()
        }
    }

    private fun createEmailPresenter(preFilledEmail: String? = null): CreateAccountEmailPresenter {
        val dataProvider = provider.createEmailDataProvider()
        val view = inflate(R.layout.include_create_account_email)
        val viewProxy = CreateAccountEmailViewProxy(view, preFilledEmail.orEmpty())
        return CreateAccountEmailPresenter(this, contactSsoAdministratorDialogFactory).apply {
            setProvider(dataProvider)
            setView(viewProxy)
        }
    }

    fun showChoosePasswordPage(username: String, inEuropeanUnion: Boolean, country: String?, isB2B: Boolean) {
        val presenter = createChoosePasswordPresenter(username, isB2B)
        pagesStateHelper.addedPage(presenter)
        pagesStateHelper.user = username
        pagesStateHelper.inEuropeanUnion = inEuropeanUnion
        pagesStateHelper.country = country
        pagesStateHelper.accountType = UserAccountInfo.AccountType.MasterPassword
        pagesStateHelper.isB2B = isB2B
    }

    private fun createChoosePasswordPresenter(username: String, isB2B: Boolean): CreateAccountChoosePasswordPresenter {
        val dataProvider = provider.createChoosePasswordDataProvider(username, isB2B)
        val view = inflate(R.layout.include_create_account_choose_password)
        val viewProxy = CreateAccountChoosePasswordViewProxy(view, coroutineScope)
        return CreateAccountChoosePasswordPresenter(this).apply {
            setProvider(dataProvider)
            setView(viewProxy)
        }
    }

    fun showConfirmPasswordPage(username: String, password: ObfuscatedByteArray) {
        val presenter =
            createConfirmPasswordPresenter(username, password)
        pagesStateHelper.addedPage(presenter)
        pagesStateHelper.password = password
    }

    private fun createConfirmPasswordPresenter(
        username: String,
        password: ObfuscatedByteArray
    ): CreateAccountConfirmPasswordPresenter {
        val dataProvider =
            provider.createConfirmPasswordDataProvider(
                username,
                password,
                pagesStateHelper.inEuropeanUnion,
                null,
                pagesStateHelper.country!!
            )
        val view = inflate(R.layout.include_create_account_confirm_password)
        val viewProxy = CreateAccountConfirmPasswordViewProxy(view)
        return CreateAccountConfirmPasswordPresenter(
            this,
            ViewModelProvider(activity as FragmentActivity)
                .getDeferredViewModel(
                    "CreateAccountActivity_PasswordSuccess"
                )
        ).apply {
            setProvider(dataProvider)
            setView(viewProxy)
        }
    }

    fun showSettingsPage() {
        val presenter = createSettingsPresenter()
        pagesStateHelper.addedPage(presenter)
    }

    private fun createSettingsPresenter(): CreateAccountSettingsPresenter {
        val view = inflate(R.layout.include_create_account_settings)
        val viewProxy = CreateAccountSettingsViewProxy(view)
        return CreateAccountSettingsPresenter(
            this
        ).apply {
            setView(viewProxy)
        }
    }

    fun showTos() {
        fragmentActivity.run {
            CreateAccountTosBottomSheetDialogFragment.newInstance(provider.isExplicitOptinRequired(pagesStateHelper.inEuropeanUnion))
                .show(supportFragmentManager, null)
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun createAccount(deferredCreateAccount: Deferred<Unit>) {
        coroutineScope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
            showProgress = true
            try {
                deferredCreateAccount.await()
            } catch (e: CancellationException) {
                
                throw e
            } catch (e: CreateAccountBaseContract.NetworkException) {
                notifyNetworkError()
                return@launch
            } catch (e: CreateAccountBaseContract.ExpiredVersionException) {
                notifyExpiredVersionError()
                return@launch
            } catch (t: Throwable) {
                notifyUnknownError()
                return@launch
            } finally {
                showProgress = false
            }

            notifySuccess()

            accountCreationHolder.deferred = null
        }
    }

    private fun listenForTosEvents() {
        fragmentActivity.run {
            val agreedTosEventChannelHolder = AgreedTosEvent.ChannelHolder.of(this)

            lifecycleScope.launch {
                for (event in agreedTosEventChannelHolder.channel) {
                    onAgreedTosEvent(event)
                }
            }
        }
    }

    private fun onAgreedTosEvent(event: AgreedTosEvent) {
        val deferredCreateAccount = accountCreationHolder.async {
            provider.createAccount(
                pagesStateHelper.user!!,
                pagesStateHelper.password!!,
                pagesStateHelper.accountType!!,
                AccountCreator.TermsState(true, event.optInOffers),
                pagesStateHelper.biometricAuthentication,
                pagesStateHelper.resetMp,
                pagesStateHelper.country!!
            )
        }
        createAccount(deferredCreateAccount)
    }

    private fun notifySuccess() {
        activity?.apply {
            startActivity(provider.createSuccessIntent())
            finishAffinity()
        }
    }

    private fun notifyExpiredVersionError() {
        activity?.run {
            endOfLife.showExpiredVersionMessaging(this)
        }
    }

    private fun notifyNetworkError() {
        view.showError(R.string.network_error)
    }

    private fun notifyUnknownError() {
        view.showError(R.string.error)
    }

    private fun inflate(@LayoutRes layoutResId: Int): View =
        provider.layoutInflater.inflate(layoutResId, view.content, true)

    private fun updateCurrentPageView() {
        val page = when (pagesStateHelper.currentPresenter) {
            is CreateAccountEmailPresenter -> AnyPage.ACCOUNT_CREATION_EMAIL
            is CreateAccountChoosePasswordPresenter -> AnyPage.ACCOUNT_CREATION_CREATE_MASTER_PASSWORD
            is CreateAccountConfirmPasswordPresenter -> AnyPage.ACCOUNT_CREATION_CONFIRM_MASTER_PASSWORD
            is CreateAccountSettingsPresenter -> AnyPage.ACCOUNT_CREATION_UNLOCK_OPTION
            else -> AnyPage.ACCOUNT_CREATION
        }

        setCurrentPageView(page)
    }

    fun toggleMplessButtonVisibility(visible: Boolean) {
        view.mplessButtonVisible = visible
    }

    private inner class PagesStateHelper {

        val pageStates = mutableListOf<CreateAccountBaseContract.Presenter>()

        var user: String? = null

        var password: ObfuscatedByteArray? = null

        var accountType: UserAccountInfo.AccountType? = null

        var isB2B: Boolean = false

        var inEuropeanUnion: Boolean = false

        var country: String? = null

        var biometricAuthentication: Boolean = false

        var resetMp: Boolean = false

        val currentPresenter
            get() = pageStates.last()

        val hasPrevious
            get() = pageStates.size > 1

        fun addedPage(presenter: CreateAccountBaseContract.Presenter) {
            val previous = pageStates.lastOrNull()
            pageStates.add(presenter)
            view.navigateNext { previous?.visible = false }
            presenter.visible = true
            view.nextEnabled = presenter.nextEnabled
            presenter.onShow()
            updateCurrentPageView()
        }

        fun removedLastPage() {
            currentPresenter.visible = false
            pageStates.removeAt(pageStates.size - 1)
            val currentPresenter = this.currentPresenter
            currentPresenter.visible = true
            view.nextEnabled = currentPresenter.nextEnabled
            currentPresenter.onShow()
            if (currentPresenter is CreateAccountEmailPresenter) {
                
                user = null
            }
            if (currentPresenter is CreateAccountChoosePasswordPresenter) {
                
                password = null
            }
            updateCurrentPageView()
        }

        fun writeState(bundle: Bundle) {
            bundle.putString(STATE_EMAIL, user)
            bundle.putBoolean(STATE_IN_EU, inEuropeanUnion)
            bundle.putString(STATE_COUNTRY, country)
            bundle.putBoolean(STATE_TOS, currentPresenter is CreateAccountSettingsContract.Presenter)
            bundle.putBoolean(STATE_BIOMETRIC, biometricAuthentication)
            bundle.putBoolean(STATE_RESETMP, resetMp)
            bundle.putBoolean(STATE_IS_B2B, isB2B)
            
        }

        fun readState(bundle: Bundle) {
            pageStates.add(createEmailPresenter())
            inEuropeanUnion = bundle.getBoolean(STATE_IN_EU)
            country = bundle.getString(STATE_COUNTRY)
            biometricAuthentication = bundle.getBoolean(STATE_BIOMETRIC)
            resetMp = bundle.getBoolean(STATE_RESETMP)
            isB2B = bundle.getBoolean(STATE_IS_B2B)
            user = bundle.getString(STATE_EMAIL)?.also { u ->
                pageStates.add(createChoosePasswordPresenter(u, isB2B))
                password?.let { pw ->
                    pageStates.add(createConfirmPasswordPresenter(u, pw))
                    if (bundle.getBoolean(STATE_TOS)) {
                        pageStates.add(createSettingsPresenter())
                    }
                }
            }
            view.navigateLast()
            val currentPresenter = this.currentPresenter
            view.nextEnabled = currentPresenter.nextEnabled
            currentPresenter.visible = true
            currentPresenter.onShow()
        }
    }

    companion object {
        private const val STATE_EMAIL = "create_account_email"
        private const val STATE_IN_EU = "in_eu"
        private const val STATE_COUNTRY = "country"
        private const val STATE_TOS = "tos"
        private const val STATE_BIOMETRIC = "biometric"
        private const val STATE_RESETMP = "resetmp"
        private const val STATE_IS_B2B = "isB2B"
    }
}