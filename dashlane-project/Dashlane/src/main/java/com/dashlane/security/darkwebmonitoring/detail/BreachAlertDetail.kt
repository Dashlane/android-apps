package com.dashlane.security.darkwebmonitoring.detail

import android.app.Activity
import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.vault.summary.SummaryObject
import com.skocken.presentation.definition.Base

interface BreachAlertDetail {

    interface Presenter : Base.IPresenter {
        fun onStart()
        fun onResume()
        fun deleteBreach(activityLifecycleScope: LifecycleCoroutineScope)
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun deleteBreach(breachWrapper: BreachWrapper)
        suspend fun restoreBreach(breachWrapper: BreachWrapper)
        suspend fun markAsViewed(breachWrapper: BreachWrapper)
        suspend fun markAsResolved(breachWrapper: BreachWrapper)
        suspend fun getDarkwebBreaches(): List<BreachWrapper>
        suspend fun getCredential(itemId: String): SummaryObject.Authentifiant?
    }

    interface ViewProxy : Base.IView {
        fun updateTitle(darkWebBreach: Boolean, isSolved: Boolean)
        fun setDomain(domain: String?)
        fun showWebsite(website: String)
        fun hideWebsite()
        fun setEmails(emails: List<String>?)
        fun setDate(date: String)
        fun setPassword(password: String)
        fun setDataInvolved(dataInvolved: String)
        fun showAdvicesInfoBox(advices: Set<BreachAlertAdvice>)
        fun showUndoDeletion(activity: Activity, onRestore: () -> Unit)
    }

    interface Logger {
        fun logDelete(breachWrapper: BreachWrapper)
        fun logCheckCredentials()
    }
}
