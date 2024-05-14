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
import com.dashlane.autofill.core.AutofillUsageLog
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.teamspaces.db.SmartSpaceCategorizationManager
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.resolveActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
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
    lateinit var smartSpaceCategorizationManager: SmartSpaceCategorizationManager

    @Inject
    lateinit var linkedServicesHelper: LinkedServicesHelper

    @Inject
    lateinit var databaseAccess: AutoFillDataBaseAccess

    @Inject
    lateinit var autofillUsageLog: AutofillUsageLog

    @Inject
    lateinit var packageNameSignatureHelper: PackageNameSignatureHelper

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var inAppLoginManager: InAppLoginManager

    override fun onCreate() {
        super.onCreate()
        homePackage = getMainHomeActivityPackageName(this)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        val analysisResultFiller = LoginFormFiller(autofillUsageLog)
        val autoFillBlackList = AutoFillBlackListImpl(arrayOf(homePackage))

        val alwaysOnUiManager = AlwaysOnUiManager(
            this,
            analysisResultFiller,
            autofillUsageLog,
            databaseAccess,
            inAppLoginManager
        )
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