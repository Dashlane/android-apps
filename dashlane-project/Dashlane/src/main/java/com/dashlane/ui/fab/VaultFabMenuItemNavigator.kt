package com.dashlane.ui.fab

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.inject.UserActivityComponent.Companion.invoke
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.useractivity.log.usage.UsageLogConstant.FabAction
import com.dashlane.useractivity.log.usage.UsageLogConstant.FabSubType
import com.dashlane.util.getBaseActivity
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType

class VaultFabMenuItemNavigator(
    private val fabViewProxy: VaultFabViewProxy,
    private val teamspaceManager: OptionalProvider<TeamspaceAccessor>,
    private val navigator: Navigator
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
        setupClickListener(rootView, R.id.fab_menu_item_paypal)
        setupClickListener(rootView, R.id.fab_menu_item_bank_account)
        setupClickListener(rootView, R.id.fab_menu_item_id_card)
        setupClickListener(rootView, R.id.fab_menu_item_passport)
        setupClickListener(rootView, R.id.fab_menu_item_driver_license)
        setupClickListener(rootView, R.id.fab_menu_item_social_security)
        setupClickListener(rootView, R.id.fab_menu_item_tax_number)
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
            R.id.fab_menu_item_paypal -> createNewPaypal()
            R.id.fab_menu_item_bank_account -> createNewBankAccount()
            R.id.fab_menu_item_id_card -> addNewIDCard()
            R.id.fab_menu_item_passport -> addNewPassport()
            R.id.fab_menu_item_driver_license -> addNewDriverLicence()
            R.id.fab_menu_item_social_security -> addNewSocialSecurity()
            R.id.fab_menu_item_tax_number -> addNewTaxStatement()
            else -> Unit
        }
    }

    fun addPassword() {
        addNewItem(SyncObjectType.AUTHENTIFIANT)
        log(
            FabSubType.layer1,
            FabAction.password,
            UsageLogCode11.Type.AUTHENTICATION
        )
    }

    fun addSecureNote() {
        teamspaceManager.get()?.startFeatureOrNotify(
            fabViewProxy.context.getBaseActivity() as FragmentActivity,
            Teamspace.Feature.SECURE_NOTES_DISABLED,
            object : TeamspaceAccessor.FeatureCall {
                override fun startFeature() {
                    addNewItem(SyncObjectType.SECURE_NOTE)
                    log(
                        FabSubType.layer1,
                        FabAction.secure_note,
                        UsageLogCode11.Type.NOTE
                    )
                }
            }
        )
    }

    private fun addNewIdentity() {
        addNewItem(SyncObjectType.IDENTITY)
        log(
            FabSubType.layer2,
            FabAction.personal_info,
            UsageLogCode11.Type.IDENTITY
        )
    }

    private fun addNewMail() {
        addNewItem(SyncObjectType.EMAIL)
        log(FabSubType.layer2, FabAction.personal_info, UsageLogCode11.Type.EMAIL)
    }

    private fun addNewPhone() {
        addNewItem(SyncObjectType.PHONE)
        log(FabSubType.layer2, FabAction.personal_info, UsageLogCode11.Type.PHONE)
    }

    private fun addNewAddress() {
        addNewItem(SyncObjectType.ADDRESS)
        log(FabSubType.layer2, FabAction.personal_info, UsageLogCode11.Type.ADDRESS)
    }

    private fun addNewCompany() {
        addNewItem(SyncObjectType.COMPANY)
        log(FabSubType.layer2, FabAction.personal_info, UsageLogCode11.Type.COMPANY)
    }

    private fun addNewWebsite() {
        addNewItem(SyncObjectType.PERSONAL_WEBSITE)
        log(FabSubType.layer2, FabAction.personal_info, UsageLogCode11.Type.WEBSITE)
    }

    fun createNewCreditCard() {
        addNewItem(SyncObjectType.PAYMENT_CREDIT_CARD)
        log(
            FabSubType.layer2,
            FabAction.payment,
            UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD
        )
    }

    private fun createNewPaypal() {
        addNewItem(SyncObjectType.PAYMENT_PAYPAL)
        log(
            FabSubType.layer2,
            FabAction.payment,
            UsageLogCode11.Type.PAYMENT_MEAN_PAYPAL
        )
    }

    private fun createNewBankAccount() {
        addNewItem(SyncObjectType.BANK_STATEMENT)
        log(
            FabSubType.layer2,
            FabAction.payment,
            UsageLogCode11.Type.BANK_STATEMENT
        )
    }

    private fun addNewIDCard() {
        addNewItem(SyncObjectType.ID_CARD)
        log(FabSubType.layer2, FabAction.ID, UsageLogCode11.Type.ID_CARD)
    }

    private fun addNewPassport() {
        addNewItem(SyncObjectType.PASSPORT)
        log(FabSubType.layer2, FabAction.ID, UsageLogCode11.Type.PASSPORT)
    }

    private fun addNewDriverLicence() {
        addNewItem(SyncObjectType.DRIVER_LICENCE)
        log(FabSubType.layer2, FabAction.ID, UsageLogCode11.Type.DRIVER_LICENCE)
    }

    private fun addNewSocialSecurity() {
        addNewItem(SyncObjectType.SOCIAL_SECURITY_STATEMENT)
        log(FabSubType.layer2, FabAction.ID, UsageLogCode11.Type.SOCIAL_SECURITY)
    }

    private fun addNewTaxStatement() {
        addNewItem(SyncObjectType.FISCAL_STATEMENT)
        log(FabSubType.layer2, FabAction.ID, UsageLogCode11.Type.FISCAL)
    }

    fun log(
        subType: String?,
        action: String?,
        subAction: UsageLogCode11.Type?
    ) {
        val usageLogRepository =
            invoke(fabViewProxy.context)
                .currentSessionUsageLogRepository
                ?: return
        FabMenuItemNavigatorLogger(usageLogRepository).log(subType, action, subAction)
    }

    private fun addNewItem(type: SyncObjectType) {
        fabViewProxy.hideFABMenu(false)
        val navigator = SingletonProvider.getNavigator()
        if (type === SyncObjectType.AUTHENTIFIANT) {
            navigator.goToCredentialAddStep1(
                UsageLogCode57.Sender.MANUAL.code
            )
        } else {
            navigator.goToCreateItem(type.desktopId)
        }
    }

    private fun setupClickListener(rootView: View, viewId: Int) {
        val view = rootView.findViewById<View>(viewId)
        view?.setOnClickListener(this)
    }
}