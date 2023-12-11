package com.dashlane.guidedonboarding

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrustFAQActivity : DashlaneActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override var requireUserUnlock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trust_faq)
        findViewById<TextView>(R.id.whatIfHackedDetail).text = toHtml(R.string.trust_faq_what_if_hacked_detail)
        findViewById<TextView>(R.id.canDashlaneSeeDetail).text = toHtml(R.string.trust_faq_can_dashlane_see_detail)
        findViewById<TextView>(R.id.howMakeMoneyDetail).text = toHtml(R.string.trust_faq_how_dashlane_make_money_detail)
        findViewById<TextView>(R.id.canILeaveDetail).text = toHtml(R.string.trust_faq_can_i_leave_detail)
        findViewById<TextView>(R.id.moreSecureDetail).text = toHtml(R.string.trust_faq_is_it_more_secure_detail)
        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
                }
            }
        )
    }

    private fun toHtml(@StringRes resId: Int) = HtmlCompat.fromHtml(getString(resId), HtmlCompat.FROM_HTML_MODE_LEGACY)
}