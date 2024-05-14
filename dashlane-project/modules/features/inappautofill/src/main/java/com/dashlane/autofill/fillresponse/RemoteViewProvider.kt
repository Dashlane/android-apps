package com.dashlane.autofill.fillresponse

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.dashlane.autofill.api.R
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface RemoteViewProvider {
    fun forItem(item: ItemToFill): RemoteViews?
    fun forOtp(): RemoteViews
    fun forLogout(): RemoteViews
    fun forViewAllItems(): RemoteViews
    fun forViewAllItemsOnEmptyResults(): RemoteViews
    fun forPause(): RemoteViews
    fun forCreateAccount(): RemoteViews
    fun forChangePassword(): RemoteViews
    fun forScrolling(): RemoteViews
    fun forEmptyWebsite(item: AuthentifiantItemToFill): RemoteViews?
    fun emptyView(): RemoteViews
}

internal open class RemoteViewProviderImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
) : RemoteViewProvider {

    override fun forItem(item: ItemToFill): RemoteViews? {
        return when (item) {
            is AuthentifiantItemToFill -> forAuthentifiant(item)
            is CreditCardItemToFill -> forCreditCard(item)
            is EmailItemToFill -> forEmail(item)
            is OtpItemToFill -> forOtp()
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

    private fun forCreditCard(item: CreditCardItemToFill): RemoteViews =
        createRemoteViews(item.cardNumberObfuscate, item.name)

    private fun forAuthentifiant(item: AuthentifiantItemToFill): RemoteViews =
        createRemoteViews(item.login, item.title)

    private fun forEmail(item: EmailItemToFill): RemoteViews =
        createRemoteViews(item.email, item.name)

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

    override fun forEmptyWebsite(item: AuthentifiantItemToFill) = forAuthentifiant(item)

    override fun emptyView() = RemoteViews(applicationContext.packageName, 0)

    internal open fun createRemoteView(packageName: String, layoutId: Int): RemoteViews {
        return RemoteViews(packageName, layoutId)
    }
}
