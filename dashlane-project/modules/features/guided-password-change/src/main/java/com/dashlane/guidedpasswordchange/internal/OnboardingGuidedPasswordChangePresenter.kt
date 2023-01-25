package com.dashlane.guidedpasswordchange.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivity
import com.dashlane.guidedpasswordchange.R
import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.url.toUrl
import com.dashlane.util.Toaster
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration

internal class OnboardingGuidedPasswordChangePresenter(
    private val domain: String,
    private val username: String?,
    private val itemUid: String,
    private val coroutineScope: CoroutineScope,
    private val navigator: Navigator,
    private val toaster: Toaster,
    private val lockHelper: LockHelper
) :
    BasePresenter<OnboardingGuidedPasswordChangeContract.DataProvider, OnboardingGuidedPasswordChangeContract.ViewProxy>(),
    OnboardingGuidedPasswordChangeContract.Presenter {

    var changePasswordUrl = domain.toUrl().toString().toUri()

    override fun onCreate(savedInstanceState: Bundle?) {
        coroutineScope.launch {
            
            provider.getPasswordChangeUrl(domain)?.let { changePasswordUrl = it }
        }
        view.currentIllustration = savedInstanceState?.getInt(STATE_CURRENT_ILLUSTRATION) ?: 0
        if (!provider.isAutofillApiEnabled(context!!)) view.showEnableAutofillApiDialog()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            outState.putInt(STATE_CURRENT_ILLUSTRATION, view.currentIllustration)
        }
    }

    override fun onStepClicked(index: Int) {
        view.currentIllustration = index
    }

    override fun onChangePasswordClicked() {
        OnboardingGuidedPasswordChangeActivity.currentDomainUsername = domain to username
        activity?.let {
            lockHelper.startAutoLockGracePeriod(Duration.ofMinutes(5))
            it.startActivityForResult(createCustomTabIntent(it), CUSTOM_TAB_REQUEST_CODE)
        }
    }

    override fun onEnableAutofillApiClicked() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE,
                Uri.parse("package:${context!!.packageName}")
            )
            activity!!.startActivityForResult(intent, ACTIVATE_AUTOFILL_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (OnboardingGuidedPasswordChangeActivity.itemUpdated && requestCode == CUSTOM_TAB_REQUEST_CODE) {
            
            navigator.goToItem(itemUid, 2)
            activity?.apply {
                toaster.show(getString(R.string.guided_password_change_success, domain), Toast.LENGTH_LONG)
                finish()
            }
        }
        if (requestCode == ACTIVATE_AUTOFILL_REQUEST_CODE && !provider.isAutofillApiEnabled(context!!)) {
            
            view.showEnableAutofillApiDialog()
        }
    }

    private fun createCustomTabIntent(context: Context) =
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .applyAppTheme()
            .setExitAnimations(
                context,
                R.anim.slide_to_left,
                R.anim.slide_to_right
            )
            .setCloseButtonIcon(getCloseButtonBitmap(context))
            .build()
            .intent
            .setData(changePasswordUrl)
            .fallbackCustomTab(context.packageManager)

    private fun getCloseButtonBitmap(context: Context) =
        AppCompatResources.getDrawable(context, R.drawable.ic_back)!!.toBitmap()

    companion object {
        private const val STATE_CURRENT_ILLUSTRATION = "current_illustration"
        private const val CUSTOM_TAB_REQUEST_CODE = 50467
        private const val ACTIVATE_AUTOFILL_REQUEST_CODE = 50468
    }
}