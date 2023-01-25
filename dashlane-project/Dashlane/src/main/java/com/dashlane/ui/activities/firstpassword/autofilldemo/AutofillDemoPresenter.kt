package com.dashlane.ui.activities.firstpassword.autofilldemo

import android.os.Bundle
import com.dashlane.navigation.Navigator
import com.dashlane.util.getImageDrawableByWebsiteUrl
import com.skocken.presentation.presenter.BasePresenter
import javax.inject.Inject

class AutofillDemoPresenter @Inject constructor(
    dataProvider: AutofillDemo.DataProvider,
    val logger: AutofillDemo.Logger,
    private val navigator: Navigator
) : BasePresenter<AutofillDemo.DataProvider, AutofillDemo.ViewProxy>(),
    AutofillDemo.Presenter {
    lateinit var login: String
    lateinit var password: String
    var finishEnabled: Boolean = false
    private lateinit var url: String

    init {
        setProvider(dataProvider)
    }

    override fun onCreate(url: String, login: String, password: String, savedInstanceState: Bundle?) {
        this.url = url
        this.login = login
        this.password = password
        logger.display()
        view.setWebsiteIcon(view.context.getImageDrawableByWebsiteUrl(url))
        finishEnabled = savedInstanceState?.getBoolean(EXTRA_FINISH_ENABLED) ?: false
        if (finishEnabled) {
            view.enableFinish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_FINISH_ENABLED, finishEnabled)
    }

    override fun onEditTextFocusAcquired(id: Int) {
        view.showAutofillSuggestion(login, url, id)
        logger.autofillShown()
    }

    override fun onAutofillTriggered() {
        view.setCredential(login, password)
        view.hideAutofillSuggestion()
        finishEnabled = true
        view.enableFinish()
        logger.autofillTriggered()
    }

    override fun onButtonFinishClicked() {
        logger.finishClicked()
        navigator.goToInAppLoginIntro()
        activity?.finish()
    }

    companion object {
        const val EXTRA_FINISH_ENABLED = "finish_enabled"
    }
}
