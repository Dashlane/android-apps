package com.dashlane.item

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.events.AppEvents
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryService
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.nfc.NfcHelper
import com.dashlane.item.subview.ViewFactory
import com.dashlane.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.SchemeUtils.getDataType
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Toaster
import com.dashlane.util.setCurrentPageView
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.util.PresenterOwner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

@AndroidEntryPoint
class ItemEditViewActivity :
    DashlaneActivity(),
    PresenterOwner.Provider<ItemEditViewPresenter, ItemEditViewContract.View> {
    @Inject
    lateinit var sharingPolicyDataProvider: SharingPolicyDataProvider

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var dataProvider: ItemEditViewDataProvider

    @Inject
    lateinit var mFollowUpNotificationDiscoveryService: FollowUpNotificationDiscoveryService

    @Inject
    lateinit var crashReporterManager: CrashReporterManager

    @Inject
    lateinit var authenticatorLogger: AuthenticatorLogger

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var passwordStrengthEvaluator: PasswordStrengthEvaluator

    @Inject
    lateinit var vaultDataQuery: VaultDataQuery

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var appEvents: AppEvents

    @Inject
    lateinit var teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator

    @Inject
    lateinit var sessionCoroutineScope: SessionCoroutineScopeRepository

    @Inject
    lateinit var frozenStateManager: FrozenStateManager

    @ApplicationCoroutineScope
    @Inject
    lateinit var applicationCoroutineScope: CoroutineScope

    @Inject
    lateinit var navigator: Navigator

    lateinit var presenterOwner: PresenterOwner<ItemEditViewPresenter, ItemEditViewContract.View>
    lateinit var nfcHelper: NfcHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenterOwner = PresenterOwner(this)
        onCreateDelegate(savedInstanceState)
        setupPresenter(savedInstanceState)
        nfcHelper = NfcHelper(this)

        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.view_coordinator_layout)
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onStart() {
        super.onStart()
        presenterOwner.presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        nfcHelper.enableDispatch()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
        nfcHelper.disableDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        presenter.onNewIntent(intent)
    }

    @Suppress("DEPRECATION")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isOutside = event.action == MotionEvent.ACTION_DOWN &&
            window.isOutOfBounds(this, event) ||
            event.action == MotionEvent.ACTION_OUTSIDE
        if (window.peekDecorView() != null && isOutside) {
            onBackPressed()
            return true
        }
        return super.onTouchEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!dataProvider.isSetup) {
            
            super.onSaveInstanceState(outState)
            return
        }
        val item = dataProvider.vaultItem
        if (item.hasBeenSaved || item.syncObject is SyncObject.SecureNote) {
            
            outState.putString(PARAM_UID, item.uid)
        }
        outState.putBoolean(PARAM_FORCE_EDIT, dataProvider.isEditMode)
        outState.putBoolean(PARAM_TOOLBAR_COLLAPSED, presenter.isToolbarCollapsed())
        outState.putBundle(PARAM_SCREEN_CONFIGURATION, dataProvider.getScreenConfiguration().toBundle())
        outState.putBundle(PARAM_ADDITIONAL_DATA, dataProvider.getAdditionalData())
        super.onSaveInstanceState(outState)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        presenter.onNewActivityLaunching(object : ItemEditViewContract.Presenter.Callback {
            override fun onCompletion() {
                super@ItemEditViewActivity.startActivityForResult(
                    intent,
                    requestCode,
                    options
                )
            }
        })
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        presenter.onNewActivityLaunching(object : ItemEditViewContract.Presenter.Callback {
            override fun onCompletion() {
                super@ItemEditViewActivity.startActivity(
                    intent,
                    options
                )
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return presenter.createMenu(
            menu = menu,
            teamspaceRestrictionNotificator = teamspaceRestrictionNotificator,
            openItemHistory = ::openItemHistory
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.selectMenuItem(item)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    override fun getContentView(): Int {
        return R.layout.activity_item_edit_view
    }

    override fun getPresenterClass(): Class<ItemEditViewPresenter> {
        return ItemEditViewPresenter::class.java
    }

    override fun newViewProxy(
        presenter: ItemEditViewPresenter,
        savedInstanceState: Bundle?,
    ): ItemEditViewContract.View {
        return ItemEditViewViewProxy(
            activity = this,
            viewFactory = ViewFactory(this, toaster, lockManager),
            lifecycleOwner = this,
            navigator = navigator,
            authenticatorLogger = authenticatorLogger,
            passwordStrengthEvaluator = passwordStrengthEvaluator,
            vaultDataQuery = vaultDataQuery
        )
    }

    val presenter: ItemEditViewPresenter
        get() = presenterOwner.presenter

    private fun onCreateDelegate(savedInstanceState: Bundle?) {
        this.contentView = contentView
        presenterOwner.createPresenter()
        presenterOwner.initViewProxy(savedInstanceState)
    }

    private fun setupPresenter(savedInstanceState: Bundle?) {
        presenter.coroutineScope =
            sessionCoroutineScope[sessionManager.session] ?: applicationCoroutineScope
        presenter.userFeaturesChecker = userFeaturesChecker
        presenter.sharingPolicyDataProvider = sharingPolicyDataProvider
        presenter.appEvents = appEvents
        presenter.frozenStateManager = frozenStateManager
        presenter.setProvider(dataProvider)
        val extras = intent.extras!!
        val args = ItemEditViewActivityArgs.fromBundle(extras)
        var screenConfig: Bundle? = null
        var additionalData: Bundle? = null
        var toolbarCollapsed = false
        var forceEdit = args.forceEdit
        var uid = args.uid
        val dataTypeName = args.dataTypeName
        val dataType: SyncObjectType = if (dataTypeName != null) {
            getDataType(dataTypeName) ?: throw IllegalArgumentException("$dataTypeName is not supported")
        } else {
            SyncObjectType.forXmlName(args.dataType)
        }
        if (savedInstanceState != null) {
            screenConfig = savedInstanceState.getBundle(PARAM_SCREEN_CONFIGURATION)
            additionalData = savedInstanceState.getBundle(PARAM_ADDITIONAL_DATA)
            toolbarCollapsed = savedInstanceState.getBoolean(PARAM_TOOLBAR_COLLAPSED, false)
            forceEdit = savedInstanceState.getBoolean(PARAM_FORCE_EDIT, forceEdit)
            uid = savedInstanceState.getString(PARAM_UID, uid)
        }
        setCurrentPageView(dataType, uid)
        val screenOptions = ItemEditViewSetupOptions(
            dataType,
            uid,
            args.url,
            toolbarCollapsed,
            forceEdit,
            args.successIntent,
            screenConfig,
            additionalData,
            args.otp
        )
        presenter.setup(this, screenOptions)

        
        if (!screenOptions.editMode) {
            displayNotificationOnboardingIfNecessary(args, dataType)
        }
    }

    private fun displayNotificationOnboardingIfNecessary(
        args: ItemEditViewActivityArgs,
        dataType: SyncObjectType,
    ) {
        if (mFollowUpNotificationDiscoveryService.canDisplayReminderScreen(args.uid)) {
            navigator.goToFollowUpNotificationDiscoveryScreen(true)
        } else if (mFollowUpNotificationDiscoveryService.canDisplayDiscoveryScreen(dataType)) {
            navigator.goToFollowUpNotificationDiscoveryScreen(false)
        }
    }

    private fun setCurrentPageView(dataType: SyncObjectType, uid: String?) {
        val page = if (uid == null) {
            getCreatePage(dataType)
        } else {
            getDetailPage(dataType)
        }
        if (page == null) return
        this.setCurrentPageView(page)
    }

    private fun openItemHistory(authentifiant: SyncObject.Authentifiant) {
        navigator.goToItemHistory(authentifiant.id!!)
    }

    companion object {
        private const val PARAM_UID = "uid"
        private const val PARAM_FORCE_EDIT = "force_edit"
        private const val PARAM_TOOLBAR_COLLAPSED = "param_toolbar_collapsed"
        private const val PARAM_SCREEN_CONFIGURATION = "param_screen_configuration"
        private const val PARAM_ADDITIONAL_DATA = "param_additional_data"
        private fun getDetailPage(dataType: SyncObjectType): AnyPage? {
            return when (dataType) {
                SyncObjectType.AUTHENTIFIANT -> AnyPage.ITEM_CREDENTIAL_DETAILS
                SyncObjectType.SECURE_NOTE -> AnyPage.ITEM_SECURE_NOTE_DETAILS
                SyncObjectType.PAYMENT_CREDIT_CARD -> AnyPage.ITEM_CREDIT_CARD_DETAILS
                SyncObjectType.BANK_STATEMENT -> AnyPage.ITEM_BANK_STATEMENT_DETAILS
                SyncObjectType.ID_CARD -> AnyPage.ITEM_ID_CARD_DETAILS
                SyncObjectType.PASSPORT -> AnyPage.ITEM_PASSPORT_DETAILS
                SyncObjectType.DRIVER_LICENCE -> AnyPage.ITEM_DRIVER_LICENCE_DETAILS
                SyncObjectType.SOCIAL_SECURITY_STATEMENT -> AnyPage.ITEM_SOCIAL_SECURITY_STATEMENT_DETAILS
                SyncObjectType.FISCAL_STATEMENT -> AnyPage.ITEM_FISCAL_STATEMENT_DETAILS
                SyncObjectType.IDENTITY -> AnyPage.ITEM_IDENTITY_DETAILS
                SyncObjectType.EMAIL -> AnyPage.ITEM_EMAIL_DETAILS
                SyncObjectType.PHONE -> AnyPage.ITEM_PHONE_DETAILS
                SyncObjectType.ADDRESS -> AnyPage.ITEM_ADDRESS_DETAILS
                SyncObjectType.COMPANY -> AnyPage.ITEM_COMPANY_DETAILS
                SyncObjectType.PERSONAL_WEBSITE -> AnyPage.ITEM_WEBSITE_DETAILS
                else -> null
            }
        }

        private fun getCreatePage(dataType: SyncObjectType): AnyPage? {
            return when (dataType) {
                SyncObjectType.AUTHENTIFIANT -> AnyPage.ITEM_CREDENTIAL_CREATE
                SyncObjectType.SECURE_NOTE -> AnyPage.ITEM_SECURE_NOTE_CREATE
                SyncObjectType.PAYMENT_CREDIT_CARD -> AnyPage.ITEM_CREDIT_CARD_CREATE
                SyncObjectType.BANK_STATEMENT -> AnyPage.ITEM_BANK_STATEMENT_CREATE
                SyncObjectType.ID_CARD -> AnyPage.ITEM_ID_CARD_CREATE
                SyncObjectType.PASSPORT -> AnyPage.ITEM_PASSPORT_CREATE
                SyncObjectType.DRIVER_LICENCE -> AnyPage.ITEM_DRIVER_LICENCE_CREATE
                SyncObjectType.SOCIAL_SECURITY_STATEMENT -> AnyPage.ITEM_SOCIAL_SECURITY_STATEMENT_CREATE
                SyncObjectType.FISCAL_STATEMENT -> AnyPage.ITEM_FISCAL_STATEMENT_CREATE
                SyncObjectType.IDENTITY -> AnyPage.ITEM_IDENTITY_CREATE
                SyncObjectType.EMAIL -> AnyPage.ITEM_EMAIL_CREATE
                SyncObjectType.PHONE -> AnyPage.ITEM_PHONE_CREATE
                SyncObjectType.ADDRESS -> AnyPage.ITEM_ADDRESS_CREATE
                SyncObjectType.COMPANY -> AnyPage.ITEM_COMPANY_CREATE
                SyncObjectType.PERSONAL_WEBSITE -> AnyPage.ITEM_WEBSITE_CREATE
                else -> null
            }
        }
    }
}