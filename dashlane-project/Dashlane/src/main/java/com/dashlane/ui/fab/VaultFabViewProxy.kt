package com.dashlane.ui.fab

import android.view.LayoutInflater
import android.view.View
import com.dashlane.R
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.ui.fab.FabViewUtil.LastMenuItemHiddenCallBack
import com.dashlane.ui.fab.FabViewUtil.hideFabMenuItems

class VaultFabViewProxy(
    rootView: View,
    teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    navigator: Navigator,
    private val passwordLimiter: PasswordLimiter
) :
    FabViewProxy(rootView) {
    private val fabMenuItemNavigator: VaultFabMenuItemNavigator =
        VaultFabMenuItemNavigator(
            fabViewProxy = this,
            teamspaceRestrictionNotificator = teamspaceRestrictionNotificator,
            navigator = navigator
        )

    private var currentFilter: Filter = Filter.ALL_VISIBLE_VAULT_ITEM_TYPES

    override fun setFilter(filter: Filter) {
        currentFilter = filter

        val textResId = when (filter) {
            Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> R.string.vault_fab_add_item
            Filter.FILTER_PASSWORD -> R.string.vault_fab_add_password
            Filter.FILTER_SECURE_NOTE -> R.string.vault_fab_add_secure_note
            Filter.FILTER_PAYMENT -> R.string.vault_fab_add_payment
            Filter.FILTER_PERSONAL_INFO -> R.string.vault_fab_add_personal_info
            Filter.FILTER_ID -> R.string.vault_fab_add_id
        }

        
        floatingButton.animate()
            .alpha(0.0f)
            .setDuration(floatingButton.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
            .withEndAction {
                floatingButton.setText(textResId)
                floatingButton.animate()
                    .setDuration(
                        floatingButton.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                    )
                    .alpha(1.0f)
            }
    }

    override fun onClick(v: View?) {
        when (currentFilter) {
            Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> toggleFABMenu { configureView() }
            Filter.FILTER_PASSWORD -> fabMenuItemNavigator.addPassword()
            Filter.FILTER_SECURE_NOTE -> fabMenuItemNavigator.addSecureNote()
            Filter.FILTER_PAYMENT -> toggleFABMenu { showMenuPayment() }
            Filter.FILTER_PERSONAL_INFO -> toggleFABMenu { showMenuPersonalInfo() }
            Filter.FILTER_ID -> toggleFABMenu { showMenuIds() }
        }
    }

    private fun configureView() {
        fabMenuHolder.removeAllViews()
        val fabMenu = LayoutInflater.from(context)
            .inflate(R.layout.fab_menu_list_recent_item, fabMenuHolder, false)
        fabMenuHolder.addView(fabMenu)
        val rootView = getRootView<View>()
        val buttonL1AddPassword =
            rootView.findViewById<View>(R.id.fab_menu_item_password)
        val buttonL1AddSecureNote =
            rootView.findViewById<View>(R.id.fab_menu_item_secure_note)
        val buttonL1AddPayment =
            rootView.findViewById<View>(R.id.fab_menu_item_payment)
        buttonL1AddPayment?.setOnClickListener { transitionMenuPayment() }
        val buttonL1AddPersonalInfo =
            rootView.findViewById<View>(R.id.fab_menu_item_personal_info)
        buttonL1AddPersonalInfo?.setOnClickListener { transitionMenuPersonalInfo() }
        val buttonL1AddIDs = rootView.findViewById<View>(R.id.fab_menu_item_ids)
        buttonL1AddIDs?.setOnClickListener { transitionMenuIDs() }
        fabMenuItemNavigator.init()
        buttonL1AddPassword.configureAsFab(
            titleRes = R.string.menu_v2_password_button,
            titleDescription = R.string.and_accessibility_add_password,
            drawableRes = R.drawable.ic_fab_menu_passwords,
            hasUpgradeBadge = passwordLimiter.isPasswordLimitReached()
        )
        buttonL1AddSecureNote.configureAsFab(
            titleRes = R.string.menu_v2_secure_notes_button,
            titleDescription = R.string.and_accessibility_add_secure_note,
            drawableRes = R.drawable.ic_fab_menu_secure_notes
        )
        buttonL1AddPayment.configureAsFab(
            R.string.menu_v2_payments_button,
            R.string.and_accessibility_add_payments,
            R.drawable.ic_fab_menu_payments
        )
        buttonL1AddPersonalInfo.configureAsFab(
            R.string.menu_v2_contact_button,
            R.string.and_accessibility_add_contact,
            R.drawable.ic_fab_menu_name
        )
        buttonL1AddIDs.configureAsFab(
            R.string.menu_v2_ids_button,
            R.string.and_accessibility_add_ids,
            R.drawable.ic_fab_menu_id
        )
    }

    private fun showMenuPayment() {
        if (fabMenuHolder.visibility == View.VISIBLE) {
            hideFABMenu(true)
        } else {
            configureViewPayment()
            setFabMenuShown(true)
        }
    }

    private fun showMenuPersonalInfo() {
        configureViewPersonalInfo()
        setFabMenuShown(true)
    }

    private fun showMenuIds() {
        configureViewIDs()
        setFabMenuShown(true)
    }

    private fun transitionMenuPayment() {
        hideFabMenuItems(
            fabMenuHolder,
            true,
            object : LastMenuItemHiddenCallBack {
                override fun onLastMenuItemHidden() {
                    configureViewPayment()
                }
            }
        )
    }

    private fun transitionMenuPersonalInfo() {
        hideFabMenuItems(
            fabMenuHolder,
            true,
            object : LastMenuItemHiddenCallBack {
                override fun onLastMenuItemHidden() {
                    configureViewPersonalInfo()
                }
            }
        )
    }

    private fun transitionMenuIDs() {
        hideFabMenuItems(
            fabMenuHolder,
            true,
            object : LastMenuItemHiddenCallBack {
                override fun onLastMenuItemHidden() {
                    configureViewIDs()
                }
            }
        )
    }

    private fun configureViewPayment() {
        fabMenuHolder.removeAllViews()
        val fabMenu = LayoutInflater.from(context).inflate(
            R.layout.fab_menu_list_payments,
            fabMenuHolder,
            false
        )
        fabMenuHolder.addView(fabMenu)
        val rootView = getRootView<View>()
        val buttonAddCreditCardView =
            rootView.findViewById<View>(R.id.fab_menu_item_credit_card)
        val buttonAddPaypalView = rootView.findViewById<View>(R.id.fab_menu_item_paypal)
        val buttonAddBankAccountView =
            rootView.findViewById<View>(R.id.fab_menu_item_bank_account)
        fabMenuItemNavigator.init()
        buttonAddCreditCardView.configureAsFab(
            R.string.creditcard,
            R.string.and_accessibility_add_credit_card,
            R.drawable.ic_fab_menu_payments
        )
        buttonAddPaypalView.configureAsFab(
            R.string.paypal,
            R.string.and_accessibility_add_paypal,
            R.drawable.ic_fab_menu_paypal
        )
        buttonAddBankAccountView.configureAsFab(
            R.string.bankstatement,
            R.string.and_accessibility_add_bank,
            R.drawable.ic_fab_menu_bank
        )
        setFabMenuItemsShown(animate = true)
    }

    private fun configureViewPersonalInfo() {
        fabMenuHolder.removeAllViews()
        val fabMenu = LayoutInflater.from(context).inflate(
            R.layout.fab_menu_list_personal_info,
            fabMenuHolder,
            false
        )
        fabMenuHolder.addView(fabMenu)
        val rootView = getRootView<View>()
        val buttonAddIdentity = rootView.findViewById<View>(R.id.fab_menu_item_identity)
        val buttonAddMail = rootView.findViewById<View>(R.id.fab_menu_item_mail)
        val buttonAddPhone = rootView.findViewById<View>(R.id.fab_menu_item_phone)
        val buttonAddAddress = rootView.findViewById<View>(R.id.fab_menu_item_address)
        val buttonAddCompany = rootView.findViewById<View>(R.id.fab_menu_item_company)
        val buttonAddWebsite = rootView.findViewById<View>(R.id.fab_menu_item_website)
        fabMenuItemNavigator.init()
        buttonAddIdentity.configureAsFab(
            R.string.identity,
            R.string.and_accessibility_add_name,
            R.drawable.ic_fab_menu_name
        )
        buttonAddMail.configureAsFab(R.string.email, R.string.and_accessibility_add_email, R.drawable.ic_fab_menu_email)
        buttonAddPhone.configureAsFab(
            R.string.phone,
            R.string.and_accessibility_add_phone_number,
            R.drawable.ic_fab_menu_phone
        )
        buttonAddAddress.configureAsFab(
            R.string.address,
            R.string.and_accessibility_add_address,
            R.drawable.ic_fab_menu_address
        )
        buttonAddCompany.configureAsFab(
            R.string.company,
            R.string.and_accessibility_add_company,
            R.drawable.ic_fab_menu_company
        )
        buttonAddWebsite.configureAsFab(
            R.string.personal_website,
            R.string.and_accessibility_add_website,
            R.drawable.ic_fab_menu_website
        )
        setFabMenuItemsShown(animate = true)
    }

    private fun configureViewIDs() {
        fabMenuHolder.removeAllViews()
        val fabMenu = LayoutInflater.from(context).inflate(
            R.layout.fab_menu_list_ids,
            fabMenuHolder,
            false
        )
        fabMenuHolder.addView(fabMenu)
        val rootView = getRootView<View>()
        val buttonAddIDCard = rootView.findViewById<View>(R.id.fab_menu_item_id_card)
        val buttonAddPassport = rootView.findViewById<View>(R.id.fab_menu_item_passport)
        val buttonAddDriverLicence =
            rootView.findViewById<View>(R.id.fab_menu_item_driver_license)
        val buttonAddSocialSecurity =
            rootView.findViewById<View>(R.id.fab_menu_item_social_security)
        val buttonAddTaxNumber =
            rootView.findViewById<View>(R.id.fab_menu_item_tax_number)
        fabMenuItemNavigator.init()
        buttonAddIDCard.configureAsFab(
            R.string.id_card,
            R.string.and_accessibility_add_id_card,
            R.drawable.ic_fab_menu_id
        )
        buttonAddPassport.configureAsFab(
            R.string.passport,
            R.string.and_accessibility_add_passport,
            R.drawable.ic_fab_menu_passport
        )
        buttonAddDriverLicence.configureAsFab(
            R.string.driver_license,
            R.string.and_accessibility_add_driver_license,
            R.drawable.ic_fab_menu_driver
        )
        buttonAddSocialSecurity.configureAsFab(
            R.string.social_security,
            R.string.and_accessibility_add_social_security,
            R.drawable.ic_fab_menu_ssn
        )
        buttonAddTaxNumber.configureAsFab(
            R.string.fiscal_statement,
            R.string.and_accessibility_add_fiscal_statement,
            R.drawable.ic_fab_menu_tax
        )
        setFabMenuItemsShown(animate = true)
    }
}