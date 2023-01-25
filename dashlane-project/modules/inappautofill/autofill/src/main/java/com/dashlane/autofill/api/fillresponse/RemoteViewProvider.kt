package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.model.CreditCardItemToFill
import com.dashlane.autofill.api.model.CreditCardSummaryItemToFill
import com.dashlane.autofill.api.model.EmailItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.model.TextItemToFill
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface RemoteViewProvider {
    fun forItem(item: ItemToFill, summary: AutoFillHintSummary): RemoteViews?
    fun forOtp(): RemoteViews
    fun forLogout(): RemoteViews
    fun forViewAllItems(): RemoteViews
    fun forViewAllItemsOnEmptyResults(): RemoteViews
    fun forPause(): RemoteViews
    fun forCreateAccount(): RemoteViews
    fun forChangePassword(): RemoteViews
    fun forScrolling(): RemoteViews
    fun forEmptyWebsite(item: AuthentifiantSummaryItemToFill, packageName: String): RemoteViews?
    fun emptyView(): RemoteViews
}



internal open class RemoteViewProviderImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val authentifiantResult: AutofillAnalyzerDef.IAutofillSecurityApplication
) : RemoteViewProvider {

    override fun forItem(item: ItemToFill, summary: AutoFillHintSummary): RemoteViews? {
        return when (item) {
            is AuthentifiantItemToFill -> forAuthentifiant(item.primaryItem.toSummary(), summary.packageName)
            is AuthentifiantSummaryItemToFill -> forAuthentifiant(item.primaryItem, summary.packageName)
            is CreditCardItemToFill -> forCreditCard(item.primaryItem.toSummary())
            is CreditCardSummaryItemToFill -> forCreditCard(item.primaryItem)
            is EmailItemToFill -> forEmail(item.primaryItem)
            is TextItemToFill -> {
                if (summary.formType == AutoFillFormType.OTP) {
                    forOtp()
                } else {
                    null
                }
            }
        }
    }

    override fun forOtp() = createRemoteView(applicationContext.packageName, R.layout.list_item_autofill_otp)

    override fun forLogout(): RemoteViews {
        return createRemoteView(applicationContext.packageName, R.layout.list_dashlane_login_item)
    }

    override fun forViewAllItemsOnEmptyResults(): RemoteViews =
        createRemoteView(applicationContext.packageName, R.layout.list_view_all_accounts_item_empty)

    override fun forViewAllItems() =
        createRemoteView(applicationContext.packageName, R.layout.list_view_all_accounts_item)

    override fun forPause(): RemoteViews =
        createRemoteView(applicationContext.packageName, R.layout.list_dashlane_pause_item)

    private fun forCreditCard(item: SummaryObject.PaymentCreditCard): RemoteViews =
        createRemoteViews(item.cardNumberObfuscate, item.name)

    private fun forAuthentifiant(item: SummaryObject.Authentifiant, packageName: String): RemoteViews {
        val signatureVerification = authentifiantResult.getSignatureVerification(applicationContext, packageName, item)

        return createRemoteViews(item.loginForUi, item.title).apply {
            if (signatureVerification.isIncorrect()) {
                setViewVisibility(R.id.autofillUnsecureWarning, View.VISIBLE)
            } else {
                setViewVisibility(R.id.autofillUnsecureWarning, View.GONE)
            }
        }
    }

    private fun forEmail(item: SummaryObject.Email): RemoteViews =
        createRemoteViews(item.email, item.emailName)

    private fun createRemoteViews(line1: String?, line2: String?): RemoteViews {
        return createRemoteView(applicationContext.packageName, R.layout.list_item_autofill_api).apply {
            if (line1 == null) {
                setViewVisibility(R.id.line1TextView, View.GONE)
            } else {
                setTextViewText(R.id.line1TextView, line1)
            }
            if (line2 == null) {
                setViewVisibility(R.id.line2TextView, View.GONE)
            } else {
                setTextViewText(R.id.line2TextView, line2)
            }
        }
    }

    override fun forCreateAccount(): RemoteViews {
        return createRemoteView(applicationContext.packageName, R.layout.list_create_account_item)
    }

    override fun forChangePassword(): RemoteViews {
        return createRemoteView(applicationContext.packageName, R.layout.list_change_password_item)
    }

    override fun forScrolling(): RemoteViews {
        return createRemoteView(applicationContext.packageName, R.layout.list_item_scrolling)
    }

    override fun forEmptyWebsite(item: AuthentifiantSummaryItemToFill, packageName: String) =
        forAuthentifiant(item.primaryItem, packageName)

    override fun emptyView() = RemoteViews(applicationContext.packageName, 0)

    internal open fun createRemoteView(packageName: String, layoutId: Int): RemoteViews {
        return RemoteViews(packageName, layoutId)
    }
}
