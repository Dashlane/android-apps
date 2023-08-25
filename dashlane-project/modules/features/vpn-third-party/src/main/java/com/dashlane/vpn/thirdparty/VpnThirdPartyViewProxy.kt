package com.dashlane.vpn.thirdparty

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.dashlane.help.HelpCenterLink
import com.dashlane.navigation.Navigator
import com.dashlane.ui.widgets.view.ExpandableCardView
import com.dashlane.ui.widgets.view.Infobox
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.setTextWithLinks
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy

class VpnThirdPartyViewProxy(
    fragment: Fragment,
    private val navigator: Navigator,
    private val clipboardCopy: ClipboardCopy,
    private val defaultEmail: String?,
    private val suggestions: List<String>?
) : BaseViewProxy<VpnThirdPartyContract.Presenter>(fragment), VpnThirdPartyContract.ViewProxy {
    private val infobox: Infobox = findViewByIdEfficient(R.id.vpn_third_party_infobox)!!
    private val title: TextView = findViewByIdEfficient(R.id.vpn_third_party_title)!!
    private val subtitle: TextView = findViewByIdEfficient(R.id.vpn_third_party_subtitle)!!
    private val button: Button = findViewByIdEfficient(R.id.vpn_third_party_button)!!
    private val accountContainer: View = findViewByIdEfficient(R.id.vpn_third_party_account)!!
    private val login: TextView = findViewByIdEfficient(R.id.vpn_third_party_account_login)!!
    private val password: TextInputLayout =
        findViewByIdEfficient(R.id.vpn_third_party_account_password)!!
    private val loginCopy: Button = findViewByIdEfficient(R.id.vpn_third_party_copy_login)!!
    private val passwordCopy: Button = findViewByIdEfficient(R.id.vpn_third_party_copy_password)!!

    private val expandableCards = listOf<ExpandableCardView>(
        
        findViewByIdEfficient(R.id.vpn_third_party_question_1)!!,
        
        findViewByIdEfficient(R.id.vpn_third_party_question_2)!!,
        
        findViewByIdEfficient(R.id.vpn_third_party_question_3)!!
    )

    init {
        setupFaqCards()
        setupInfobox()
    }

    override fun showInfobox() {
        infobox.isVisible = true
    }

    override fun showActivate() {
        title.text = context.getString(R.string.vpn_third_party_activate_title)
        subtitle.text = context.getString(R.string.vpn_third_party_activate_subtitle)
        button.apply {
            text = context.getString(R.string.vpn_third_party_activate_button)
            setOnClickListener {
                navigator.goToActivateAccountFromVpnThirdParty(defaultEmail, suggestions)
                presenter.onActivateClicked()
            }
        }
        accountContainer.isVisible = false
    }

    override fun showAccount(username: String, password: String) {
        accountContainer.isVisible = true
        login.text = username
        this.password.editText!!.setText(password)
        title.text = context.getString(R.string.vpn_third_party_all_set_title)
        subtitle.text = context.getString(R.string.vpn_third_party_all_set_subtitle)
        button.apply {
            text = context.getString(R.string.vpn_third_party_install_app_button)
            setOnClickListener {
                presenter.onInstallAppClicked()
            }
        }
        loginCopy.setOnClickListener {
            clipboardCopy.copyToClipboard(username, false)
            presenter.onCopyLoginClicked()
        }
        passwordCopy.setOnClickListener {
            clipboardCopy.copyToClipboard(password, true)
            presenter.onCopyPasswordClicked()
        }
    }

    override fun showLaunchAppButton() {
        button.apply {
            text = context.getString(R.string.vpn_third_party_launch_app_button)
            setOnClickListener {
                presenter.onLaunchAppClicked()
            }
        }
    }

    override fun showGettingStarted() {
        navigator.goToGetStartedFromVpnThirdParty()
    }

    private fun showLearnMoreBottomSheet() {
        navigator.addOnDestinationChangedListener(object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                infobox.isVisible = false
                navigator.removeOnDestinationChangedListener(this)
            }
        })
        navigator.goToLearnMoreAboutVpnFromVpnThirdParty()
    }

    private fun setupFaqCards() {
        expandableCards.forEach { expandableCard ->
            expandableCard.setOnExpandListener { expanded ->
                if (!expanded) return@setOnExpandListener
                
                expandableCards.filter { it.id != expandableCard.id }.forEach {
                    it.setExpanded(expanded = false, withAnimation = true)
                }
            }
        }
        
        expandableCards[0].findViewById<Button>(R.id.vpn_third_party_faq_question_button)
            .setOnClickListener {
                presenter.onQuestionOneReadMoreClicked()
            }
        
        expandableCards[2].findViewById<TextView>(R.id.vpn_third_party_faq_question_body)
            .setTextWithLinks(
                R.string.vpn_third_party_faq_question_3_body,
                listOf(
                    R.string.vpn_third_party_faq_question_3_link_faq to HelpCenterLink.ARTICLE_THIRD_PARTY_VPN_FAQ.uri,
                    R.string.vpn_third_party_faq_question_3_link_hotspot_shield_support to "https://support.hotspotshield.com".toUri()
                )
            )
    }

    private fun setupInfobox() {
        infobox.primaryButton.setOnClickListener {
            showLearnMoreBottomSheet()
            presenter.onLearnMoreClicked()
        }
        infobox.secondaryButton.setOnClickListener {
            infobox.isVisible = false
            presenter.onDismissClicked()
        }
    }
}