package com.dashlane.ui.fab

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.ui.Feature
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.util.getBaseActivity
import com.dashlane.xml.domain.SyncObjectType

class VaultFabMenuItemNavigator(
    private val fabViewProxy: VaultFabViewProxy,
    private val teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    private val navigator: Navigator,
) : View.OnClickListener {
    fun init() {
        val rootView = fabViewProxy.getRootView<View>()
        setupClickListener(rootView, R.id.fab_menu_item_password)
        setupClickListener(rootView, R.id.fab_menu_item_secure_note)
        setupClickListener(rootView, R.id.fab_menu_item_identity)
        setupClickListener(rootView, R.id.fab_menu_item_mail)
        setupClickListener(rootView, R.id.fab_menu_item_phone)
        setupClickListener(rootView, R.id.fab_menu_item_address)
        setupClickListener(rootView, R.id.fab_menu_item_company)
        setupClickListener(rootView, R.id.fab_menu_item_website)
        setupClickListener(rootView, R.id.fab_menu_item_credit_card)
        setupClickListener(rootView, R.id.fab_menu_item_bank_account)
        setupClickListener(rootView, R.id.fab_menu_item_id_card)
        setupClickListener(rootView, R.id.fab_menu_item_passport)
        setupClickListener(rootView, R.id.fab_menu_item_driver_license)
        setupClickListener(rootView, R.id.fab_menu_item_social_security)
        setupClickListener(rootView, R.id.fab_menu_item_tax_number)
        setupClickListener(rootView, R.id.fab_menu_item_secret)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_menu_item_password -> addPassword()
            R.id.fab_menu_item_secure_note -> addSecureNote()
            R.id.fab_menu_item_identity -> addNewIdentity()
            R.id.fab_menu_item_mail -> addNewMail()
            R.id.fab_menu_item_phone -> addNewPhone()
            R.id.fab_menu_item_address -> addNewAddress()
            R.id.fab_menu_item_company -> addNewCompany()
            R.id.fab_menu_item_website -> addNewWebsite()
            R.id.fab_menu_item_credit_card -> createNewCreditCard()
            R.id.fab_menu_item_bank_account -> createNewBankAccount()
            R.id.fab_menu_item_id_card -> addNewIDCard()
            R.id.fab_menu_item_passport -> addNewPassport()
            R.id.fab_menu_item_driver_license -> addNewDriverLicence()
            R.id.fab_menu_item_social_security -> addNewSocialSecurity()
            R.id.fab_menu_item_tax_number -> addNewTaxStatement()
            R.id.fab_menu_item_secret -> addSecret()
            else -> Unit
        }
    }

    fun addPassword() {
        addNewItem(SyncObjectType.AUTHENTIFIANT)
    }

    fun addSecureNote() {
        teamspaceRestrictionNotificator.runOrNotifyTeamRestriction(
            fabViewProxy.context.getBaseActivity() as FragmentActivity,
            Feature.SECURE_NOTES_DISABLED
        ) {
            addNewItem(SyncObjectType.SECURE_NOTE)
        }
    }

    fun addSecret() {
        addNewItem(SyncObjectType.SECRET)
    }

    private fun addNewIdentity() {
        addNewItem(SyncObjectType.IDENTITY)
    }

    private fun addNewMail() {
        addNewItem(SyncObjectType.EMAIL)
    }

    private fun addNewPhone() {
        addNewItem(SyncObjectType.PHONE)
    }

    private fun addNewAddress() {
        addNewItem(SyncObjectType.ADDRESS)
    }

    private fun addNewCompany() {
        addNewItem(SyncObjectType.COMPANY)
    }

    private fun addNewWebsite() {
        addNewItem(SyncObjectType.PERSONAL_WEBSITE)
    }

    fun createNewCreditCard() {
        addNewItem(SyncObjectType.PAYMENT_CREDIT_CARD)
    }

    private fun createNewBankAccount() {
        addNewItem(SyncObjectType.BANK_STATEMENT)
    }

    private fun addNewIDCard() {
        addNewItem(SyncObjectType.ID_CARD)
    }

    private fun addNewPassport() {
        addNewItem(SyncObjectType.PASSPORT)
    }

    private fun addNewDriverLicence() {
        addNewItem(SyncObjectType.DRIVER_LICENCE)
    }

    private fun addNewSocialSecurity() {
        addNewItem(SyncObjectType.SOCIAL_SECURITY_STATEMENT)
    }

    private fun addNewTaxStatement() {
        addNewItem(SyncObjectType.FISCAL_STATEMENT)
    }

    private fun addNewItem(type: SyncObjectType) {
        fabViewProxy.hideFABMenu(false)
        if (type === SyncObjectType.AUTHENTIFIANT) {
            navigator.goToCredentialAddStep1()
        } else {
            navigator.goToCreateItem(type.xmlObjectName)
        }
    }

    private fun setupClickListener(rootView: View, viewId: Int) {
        val view = rootView.findViewById<View>(viewId)
        view?.setOnClickListener(this)
    }
}