package com.dashlane.autofill.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import com.dashlane.autofill.AutoFillBlackListImpl
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.autofill.accessibility.alwayson.AlwaysOnEventHandler
import com.dashlane.autofill.accessibility.alwayson.AlwaysOnUiManager
import com.dashlane.autofill.accessibility.filler.LoginFormFiller
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.util.resolveActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject



@AndroidEntryPoint
class DashlaneAccessibilityService : AccessibilityService() {

    private val evenValidator = EventValidator(this)
    private var powerManager: PowerManager? = null
    private var evenHandler: AccessibilityEventHandler? = null
    private val browserDetectionHelper = BrowserDetectionHelper
    private val accessibilityApiServiceDetector = AccessibilityApiServiceDetectorImpl(this, browserDetectionHelper)

    private var homePackage: String? = null

    @Inject
    lateinit var teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager

    @Inject
    lateinit var linkedServicesHelper: LinkedServicesHelper

    override fun onCreate() {
        super.onCreate()
        homePackage = getMainHomeActivityPackageName(this)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        val analysisResultFiller = LoginFormFiller(SingletonProvider.getComponent().autofillUsageLog)
        val autoFillBlackList = AutoFillBlackListImpl(arrayOf(homePackage))

        val mainDataAccessor = SingletonProvider.getMainDataAccessor()
        val sessionProvider = SingletonProvider.getSessionManager()
        val databaseAccess = AutoFillDataBaseAccess(
            this,
            sessionProvider,
            mainDataAccessor,
            Dispatchers.IO,
            teamspaceForceCategorizationManager,
            linkedServicesHelper
        )
        val notificationHelper = SingletonProvider.getNotificationHelper()
        val autofillUsageLog = SingletonProvider.getComponent().autofillUsageLog

        val alwaysOnUiManager =
            AlwaysOnUiManager(this, analysisResultFiller, notificationHelper, autofillUsageLog, databaseAccess)
        val alwaysOnEventHandler = AlwaysOnEventHandler(
            alwaysOnUiManager,
            autoFillBlackList,
            accessibilityApiServiceDetector,
            databaseAccess,
            browserDetectionHelper
        )

        evenHandler = alwaysOnEventHandler

        eventHandlerRef = WeakReference(alwaysOnEventHandler)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (powerManager?.isInteractive != true) {
            return 
        }
        event?.takeIf { evenValidator.isValid(it) } ?: return 
        evenHandler?.onNewEvent(event)
    }

    override fun onInterrupt() {
        
    }

    private fun getMainHomeActivityPackageName(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = context.packageManager.resolveActivityCompat(intent, 0)
        return resolveInfo?.activityInfo?.packageName
    }

    companion object {
        private var eventHandlerRef: WeakReference<AccessibilityEventHandler>? = null

        @JvmStatic
        val eventHandler: AccessibilityEventHandler?
            get() = eventHandlerRef?.get()
    }
}