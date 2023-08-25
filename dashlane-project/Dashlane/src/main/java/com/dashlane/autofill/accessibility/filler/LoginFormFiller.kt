package com.dashlane.autofill.accessibility.filler

import android.os.Bundle
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.accessibility.AutoFillAccessibilityViewNode
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.formdetector.model.AccessibilityLoginForm
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.urlDomain
import com.dashlane.xml.domain.SyncObject
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

class LoginFormFiller(private val autofillUsageLog: AutofillAnalyzerDef.IAutofillUsageLog) {

    fun getFocusOn(loginForm: AccessibilityLoginForm): Boolean {
        return try {
            if (isFocused(loginForm.password) || isFocused(loginForm.login)) {
                return true
            }
            
            performClick(loginForm.password) || performClick(loginForm.login)
        } catch (e: Exception) {
            false
        }
    }

    fun fill(loginForm: AccessibilityLoginForm, authentifiant: SyncObject.Authentifiant): Boolean {
        try {
            logEvent(loginForm, authentifiant)

            val performFillLoginField = fill(loginForm.login, authentifiant.loginForUi)

            if (performFillLoginField) {
                
                
                Thread.sleep(SLEEP_BETWEEN_ACTIONS)
            }

            val performFillPasswordField = fill(loginForm.password, authentifiant.password)

            return performFillPasswordField || performFillLoginField
        } catch (e: Exception) {
            return false
        }
    }

    private fun fill(nodeInfo: AutoFillViewNode?, value: CharSequence?): Boolean {
        if (isNodeValid(nodeInfo)) return false
        (nodeInfo as AutoFillAccessibilityViewNode).performClick()
        val bundle = Bundle()
        bundle.putCharSequence(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
        return nodeInfo.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, bundle)
    }

    private fun performClick(nodeInfo: AutoFillViewNode?): Boolean {
        if (isNodeValid(nodeInfo)) return false
        return (nodeInfo as AutoFillAccessibilityViewNode).performClick()
    }

    private fun isFocused(nodeInfo: AutoFillViewNode?): Boolean {
        if (isNodeValid(nodeInfo)) return false
        return (nodeInfo as AutoFillAccessibilityViewNode).isFocused
    }

    private fun isNodeValid(nodeInfo: AutoFillViewNode?): Boolean {
        if (nodeInfo !is AutoFillAccessibilityViewNode || !nodeInfo.refresh()) {
            return true
        }
        return false
    }

    private fun logEvent(loginForm: AccessibilityLoginForm, authentifiant: SyncObject.Authentifiant) {
        autofillUsageLog.onAutoFillCredentialDone(
            origin = AutofillOrigin.IN_APP_LOGIN,
            packageName = loginForm.packageName,
            websiteUrlDomain = loginForm.websiteUrl?.toUrlDomainOrNull(),
            itemUrlDomain = authentifiant.urlDomain?.toUrlDomainOrNull(),
            autofillFeature = AutofillFeature.SUGGESTION,
            matchType = MatchType.REGULAR,
            autofillOrigin = HermesAutofillOrigin.DROPDOWN,
            autofillMechanism = AutofillMechanism.ANDROID_ACCESSIBILITY
        )
    }

    companion object {
        private const val SLEEP_BETWEEN_ACTIONS = 200L
    }
}