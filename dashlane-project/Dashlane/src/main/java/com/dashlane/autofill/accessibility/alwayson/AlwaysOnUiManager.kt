package com.dashlane.autofill.accessibility.alwayson

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.accessibility.AutoFillAccessibilityViewNode
import com.dashlane.autofill.accessibility.DashlaneAccessibilityService
import com.dashlane.autofill.accessibility.filler.LoginFormFiller
import com.dashlane.autofill.formdetector.model.AccessibilityLoginForm
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.DashlaneBubble
import com.dashlane.ui.InAppLoginWindow
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.notificationBuilder
import com.dashlane.util.toBitmap
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import wei.mark.standout.StandOutWindow
import java.time.Instant

class AlwaysOnUiManager(
    private val context: Context,
    private val filler: LoginFormFiller,
    private val notificationHelper: NotificationHelper,
    private val autofillUsageLog: AutofillAnalyzerDef.IAutofillUsageLog,
    private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
) {

    var loginFormGetter: LoginFormGetter? = null
    var pendingAuthentifiant: SyncObject.Authentifiant? = null

    var lastScanResult: AlwaysOnEventHandler.ScanResult? = null
        private set

    private var bubbleAt: Rect? = null

    fun getFocusOnField() {
        loginFormGetter?.getLastLoginFormFound()?.let { filler.getFocusOn(it) }
    }

    fun onFormScan(scanResult: AlwaysOnEventHandler.ScanResult) {
        lastScanResult = scanResult
        val loginForm = scanResult.loginForm
        if (loginForm == null) {
            hideBubble()
        } else {
            autofillUsageLog.onLoginFormFound(loginForm.packageName)
            pendingAuthentifiant?.let {
                filler.fill(loginForm, it)
                pendingAuthentifiant = null
            }
            showBubble(loginForm, scanResult.authentifiants)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onItemPicked(uid: String) {
        val authentifiant: SyncObject.Authentifiant =
            databaseAccess.loadSyncObject<SyncObject.Authentifiant>(uid)?.syncObject ?: return
        authentifiant.id?.let { itemId ->
            GlobalScope.launch(Dispatchers.IO) {
                databaseAccess.updateLastViewDate(itemId, Instant.now())
            }
        }

        onItemPicked(authentifiant)
    }

    fun onItemPicked(authentifiant: SyncObject.Authentifiant) {
        val success = loginFormGetter?.getLastLoginFormFound()?.let { filler.fill(it, authentifiant) }
            ?: false
        pendingAuthentifiant = if (success) {
            null
        } else {
            authentifiant
        }
    }

    private fun showBubble(loginForm: AccessibilityLoginForm, authentifiants: List<SummaryObject.Authentifiant>?) {
        if (requireOverlayPermission()) {
            return
        }
        val bubblePosition = getBubblePositionFromAnalysisResult(loginForm)
        if (bubbleAt == bubblePosition) {
            return 
        }
        bubbleAt = bubblePosition

        val loggedin = authentifiants != null
        val results = authentifiants?.size ?: 0
        val packageName = loginForm.packageName

        val formFieldPosition = Bundle().apply {
            putParcelable(DashlaneBubble.DATA_FORM_FIELD_BOUND, bubblePosition)
        }

        val controllerBundle = Bundle().apply {
            putInt(
                DashlaneBubble.DATA_CONTROLLER_TYPE,
                if (loggedin) DashlaneBubble.CONTROLLER_IN_APP_LOGIN else DashlaneBubble.CONTROLLER_LOGGED_OUT
            )
            putInt(DashlaneBubble.DATA_ANALYSIS_RESULT_COUNT, results)
            putString(DashlaneBubble.DATA_ANALYSIS_RESULT_APP, packageName)
        }

        if (loggedin) {
            autofillUsageLog.onShowCredentialsList(
                origin = AutofillOrigin.IN_APP_LOGIN,
                packageName = packageName,
                isNativeApp = loginForm.websiteUrl != null,
                totalCount = results
            )
        }

        val bubbleClass = DashlaneBubble::class.java
        StandOutWindow.show(context, bubbleClass, DashlaneBubble.WINDOW_ID)
        StandOutWindow.sendData(
            context,
            bubbleClass,
            DashlaneBubble.WINDOW_ID,
            DashlaneBubble.REQUEST_CODE_CHANGE_CONTROLLER,
            controllerBundle,
            bubbleClass,
            0
        )
        StandOutWindow.sendData(
            context,
            bubbleClass,
            DashlaneBubble.WINDOW_ID,
            DashlaneBubble.REQUEST_CODE_MOVE_TO_FIELD,
            formFieldPosition,
            bubbleClass,
            0
        )
    }

    private fun hideBubble() {
        if (bubbleAt == null) {
            return 
        }
        bubbleAt = null 

        if (isDashlaneAccessibilityRunning()) {
            StandOutWindow.hide(context, DashlaneBubble::class.java, DashlaneBubble.WINDOW_ID)
            StandOutWindow.hide(context, InAppLoginWindow::class.java, InAppLoginWindow.WINDOW_ID)
        }
    }

    private fun getBubblePositionFromAnalysisResult(loginForm: AccessibilityLoginForm): Rect {
        val bounds = Rect(0, 0, 0, 0)
        (loginForm.password as? AutoFillAccessibilityViewNode)?.getBoundsInScreen(bounds)
        return bounds
    }

    @Suppress("DEPRECATION")
    private fun isDashlaneAccessibilityRunning(): Boolean {
        
        
        return (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.getRunningServices(Integer.MAX_VALUE)
            ?.any { DashlaneAccessibilityService::class.java.name == it?.service?.className }
            ?: return false
    }

    private fun requireOverlayPermission(): Boolean {
        val intent = SingletonProvider.getInAppLoginManager().intentOverlayPermissionIfRequire

        val notificationManager = NotificationManagerCompat.from(context)

        if (intent == null) {
            
            notificationManager.cancel(NOTIFICATION_ID_REQUEST_OVERDRAW_PERMISSION)
            return false
        }

        
        val pIntent =
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        
        val icon = ContextCompat.getDrawable(context, R.drawable.in_app_login_bubble)!!.toBitmap()

        val notification = notificationBuilder(context) {
            setSmallIcon(R.drawable.in_app_login)
            setLargeIcon(icon)
            setContentTitle(context.getString(R.string.m_request_draw_overlays_title))
            setContentText(context.getString(R.string.m_request_draw_overlays_body), true)
            setAutoCancel()
            setLocalOnly()
            setOnlyAlertOnce()
            setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            setChannel(NotificationHelper.Channel.SECURITY)
            setContentIntent(pIntent)
        }.build()

        
        try {
            notificationManager.notify(NOTIFICATION_ID_REQUEST_OVERDRAW_PERMISSION, notification)
        } catch (e: SecurityException) {
            
        }
        return true
    }

    interface LoginFormGetter {
        fun getLastLoginFormFound(): AccessibilityLoginForm?
    }

    companion object {
        private const val NOTIFICATION_ID_REQUEST_OVERDRAW_PERMISSION = 10001
    }
}