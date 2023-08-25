package com.dashlane.ui.activities.firstpassword

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.dashlane.core.DataSync
import com.dashlane.guidedonboarding.R
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoActivity
import com.dashlane.ui.activities.firstpassword.faq.FAQFirstPasswordActivity
import com.dashlane.util.getImageDrawableByWebsiteUrl
import com.dashlane.util.getSerializableCompat
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.startActivity
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddFirstPasswordPresenter @Inject constructor(
    dataProvider: AddFirstPassword.DataProvider,
    val logger: AddFirstPasswordLogger,
    private val navigator: Navigator,
    private val dataSync: DataSync
) : BasePresenter<AddFirstPassword.DataProvider, AddFirstPassword.ViewProxy>(),
    AddFirstPassword.Presenter {

    private var displayAutofillDemo = false
    private lateinit var url: String
    private lateinit var savedLogin: String
    private lateinit var savedPassword: SyncObfuscatedValue

    init {
        setProvider(dataProvider)
    }

    override fun onCreate(url: String, savedInstanceState: Bundle?) {
        this.url = url
        view.setupToolbar(view.context.getImageDrawableByWebsiteUrl(url))
        prefillLogin()
        logger.display()

        displayAutofillDemo = savedInstanceState?.getBoolean(EXTRA_DISPLAY_AUTOFILL_PROMPT) ?: false
        savedInstanceState?.getString(EXTRA_SAVED_LOGIN)?.let { savedLogin = it }
        savedInstanceState?.getSerializableCompat<SyncObfuscatedValue>(EXTRA_SAVED_PASSWORD)?.let { savedPassword = it }

        if (displayAutofillDemo) {
            view.displayAutofillDemoPrompt()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_DISPLAY_AUTOFILL_PROMPT, displayAutofillDemo)
        if (::savedLogin.isInitialized) {
            outState.putString(EXTRA_SAVED_LOGIN, savedLogin)
        }
        if (::savedPassword.isInitialized) {
            outState.putSerializable(EXTRA_SAVED_PASSWORD, savedPassword)
        }
    }

    override fun onDestroy() {
        view.dismissAutofillDemoPrompt()
    }

    override fun onButtonSecureClicked() {
        logger.onClickSecureButton()
        context?.startActivity<FAQFirstPasswordActivity>(R.anim.slide_in_bottom, R.anim.no_animation) {}
    }

    override fun onButtonSaveClicked(login: String, password: String) {
        if (displayAutofillDemo) {
            return
        }

        logger.onClickSaveButton()
        val credential = provider.createCredential(url, login, password)
        
        viewModelScope.launch(Dispatchers.Main) {
            if (provider.saveCredential(credential)) {
                savedLogin = login
                savedPassword = password.toSyncObfuscatedValue()
                logger.onCredentialSaved(credential)
                displayAutofillDemo = true
                view.displayAutofillDemoPrompt()
                dataSync.sync(Trigger.SAVE)
            }
        }
    }

    override fun onButtonTryDemoClicked() {
        logger.onClickTryDemo()
        activity?.let {
            val intent = AutofillDemoActivity.newIntent(it, url, savedLogin, savedPassword)
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun onButtonReturnHomeClicked() {
        logger.onClickReturnHome()
        view.dismissAutofillDemoPrompt()
        navigator.goToPersonalPlanOrHome()
        activity?.finish()
    }

    private fun prefillLogin() {
        provider.sessionEmail?.let {
            view.setLogin(it)
        }
    }

    companion object {
        private const val EXTRA_DISPLAY_AUTOFILL_PROMPT = "display_autofill_prompt"
        private const val EXTRA_SAVED_LOGIN = "saved_login"
        private const val EXTRA_SAVED_PASSWORD = "saved_password"
    }
}
