package com.dashlane.autofill.accessibility.alwayson

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.toAutoFillViewNode
import com.dashlane.autofill.AutoFillBlackList
import com.dashlane.autofill.AutoFillBlackListImpl
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.accessibility.AccessibilityApiServiceDetector
import com.dashlane.autofill.accessibility.AccessibilityEventHandler
import com.dashlane.autofill.api.util.AutofillValueFactoryAndroidImpl
import com.dashlane.autofill.formdetector.AutoFillHintsExtractor
import com.dashlane.autofill.formdetector.AutofillPackageNameAcceptor
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.formdetector.model.AccessibilityLoginForm
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.util.thread.ConflatedQueueExecutor
import com.dashlane.vault.summary.SummaryObject



class AlwaysOnEventHandler(
    val alwaysOnUiManager: AlwaysOnUiManager,
    private val blackList: AutoFillBlackList,
    private val accessibilityApiServiceDetector: AccessibilityApiServiceDetector,
    private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess,
    private val browserDetectionHelper: BrowserDetectionHelper
) : AccessibilityEventHandler, AlwaysOnUiManager.LoginFormGetter {

    private val handler = Handler(Looper.myLooper()!!)
    private var pendingEventToScan: AutoFillViewNode? = null
    private var lastLoginForm: AccessibilityLoginForm? = null

    private val executor = ConflatedQueueExecutor()

    init {
        alwaysOnUiManager.loginFormGetter = this
    }

    override fun onNewEvent(event: AccessibilityEvent) {
        val autoFillViewNode = event.toAutoFillViewNode() ?: return
        pendingEventToScan = autoFillViewNode
        startThreadIfRequire()
    }

    override fun getLastLoginFormFound() = lastLoginForm

    private fun startThreadIfRequire() {
        executor.execute {
            val event = pendingEventToScan ?: return@execute
            val packageName = event.packageName?.toString() ?: return@execute
            val mBlacklistPackages = AutoFillBlackListImpl()
            val autofillValueFactory = AutofillValueFactoryAndroidImpl()
            val packageNameAcceptor = AutofillPackageNameAcceptor(
                mBlacklistPackages,
                accessibilityApiServiceDetector
            )
            val autoFillHintsExtractor = AutoFillHintsExtractor(autofillValueFactory, packageNameAcceptor)
            val loginForm = if (blackList.isBlackList(packageName) ||
                !accessibilityApiServiceDetector.shouldUseAccessibilityIfEnable(packageName)
            ) {
                if (packageName == "com.dashlane") {
                    databaseAccess.clearCache()
                }
                null
            } else {
                autoFillHintsExtractor.extractForAccessibilityService(event, packageName)
            }?.takeIf { it.password != null }?.let { getLoginFormFixedForBrowser(it) }

            val authentifiants = loginForm?.let {
                if (it.websiteUrl.isNullOrBlank()) {
                    databaseAccess.loadAuthentifiantsByPackageName(packageName)
                } else {
                    databaseAccess.loadAuthentifiantsByUrl(it.websiteUrl!!)
                }
            }

            handler.post {
                
                onResultForm(loginForm, authentifiants)
            }

            
            Thread.sleep(WAIT_BETWEEN_SCAN_EVENTS)
        }
    }

    private fun getLoginFormFixedForBrowser(loginForm: AccessibilityLoginForm): AccessibilityLoginForm? {
        if (loginForm.websiteUrl != null) {
            return loginForm 
        }
        if (!browserDetectionHelper.isBrowserSupported(loginForm.packageName)) {
            return loginForm 
        }
        return lastLoginForm
            
            ?.takeIf { it.packageName == loginForm.packageName }
            
            ?.takeIf { it.login?.viewIdResourceName != null || it.password?.viewIdResourceName != null }
            
            ?.takeIf { it.login?.viewIdResourceName == loginForm.login?.viewIdResourceName }
            
            ?.takeIf { it.password?.viewIdResourceName == loginForm.password?.viewIdResourceName }
            
            ?.websiteUrl
            
            ?.let { loginForm.copy(websiteUrl = it) }
    }

    private fun onResultForm(loginForm: AccessibilityLoginForm?, authentifiants: List<SummaryObject.Authentifiant>?) {
        if (loginForm != null) {
            lastLoginForm = loginForm
        }
        alwaysOnUiManager.onFormScan(
            ScanResult(
                loginForm,
                authentifiants?.sortedByDescending {
                    it.locallyViewedDate ?: it.modificationDatetime ?: it.creationDatetime
                })
        )
    }

    data class ScanResult(
        val loginForm: AccessibilityLoginForm?,
        val authentifiants: List<SummaryObject.Authentifiant>?
    )

    companion object {
        const val WAIT_BETWEEN_SCAN_EVENTS: Long = 100
    }
}