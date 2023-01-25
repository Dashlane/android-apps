package com.dashlane.login.sso

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.R
import com.dashlane.createaccount.pages.tos.AgreedTosEvent
import com.dashlane.createaccount.pages.tos.CreateAccountTosBottomSheetDialogFragment
import com.dashlane.util.getThemeAttrColor
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlinx.coroutines.launch

class LoginSsoViewProxy(
    private val fragmentActivity: FragmentActivity
) : BaseViewProxy<LoginSsoContract.Presenter>(fragmentActivity), LoginSsoContract.ViewProxy {
    private val lottieView = findViewByIdEfficient<LottieAnimationView>(R.id.lottie)!!.apply {
        val color = context.getThemeAttrColor(R.attr.colorOnBackground)

        addValueCallback(KeyPath("load", "**"), LottieProperty.STROKE_COLOR) { color }
        addValueCallback(KeyPath("load 2", "**"), LottieProperty.STROKE_COLOR) { color }
    }

    private val messageView = findViewByIdEfficient<View>(R.id.message)!!

    init {
        fragmentActivity.lifecycleScope.launch {
            for (event in AgreedTosEvent.ChannelHolder.of(fragmentActivity).channel) {
                presenterOrNull?.onTermsAgreed(optInOffers = event.optInOffers)
            }
        }
    }

    override fun showLoading() {
        lottieView.isVisible = true
        messageView.isVisible = true
    }

    override fun showTerms() {
        val fm = fragmentActivity.supportFragmentManager

        val createAccountTosBottomSheet =
            fm.findFragmentByTag(FRAGMENT_TAG_CREATE_ACCOUNT_TOS) as CreateAccountTosBottomSheetDialogFragment?
                ?: CreateAccountTosBottomSheetDialogFragment.newInstance(requireExplicitOptin = true)

        if (!createAccountTosBottomSheet.isAdded) {
            createAccountTosBottomSheet.isCancelable = false
            createAccountTosBottomSheet.show(fm, FRAGMENT_TAG_CREATE_ACCOUNT_TOS)
        }
    }

    companion object {
        private const val FRAGMENT_TAG_CREATE_ACCOUNT_TOS = "create_account_tos"
    }
}