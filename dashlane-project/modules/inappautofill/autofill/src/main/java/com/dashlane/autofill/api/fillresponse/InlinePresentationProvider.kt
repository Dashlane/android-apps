package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.InlinePresentation
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.core.content.ContextCompat.getColor
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.model.CreditCardSummaryItemToFill
import com.dashlane.autofill.api.model.EmailItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface InlinePresentationProvider {
    

    fun forItem(item: ItemToFill, spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forLogout(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forOnBoarding(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forViewAllItems(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forViewAllItemsOnEmptyResults(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forPause(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forAddAccount(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forChangePassword(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forSmsOtp(spec: InlinePresentationSpec?): InlinePresentation?

    

    fun forPinnedItem(spec: InlinePresentationSpec?): InlinePresentation?
}



@RequiresApi(Build.VERSION_CODES.R)
internal class InlinePresentationProviderImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    navigationService: AutofillNavigationService
) : InlinePresentationProvider {

    private val longPressIntent = navigationService.getLongPressActionOnInline()
    private val inlineStringProvider = InlineSuggestionStringProvider(applicationContext)

    private val iconViewAllAccount
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_view_all_account).applyTint()
    private val iconAddAccount
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_add_account).applyTint()
    private val iconChangePassword
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_change_password).applyTint()
    private val iconDashlane
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_pinned).applyTint()
    private val iconOtp
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_sms_otp).applyTint()
    private val iconOnBoarding
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_inline_onboarding).applyTint()

    override fun forItem(
        item: ItemToFill,
        spec: InlinePresentationSpec?
    ) = when (item) {
        is AuthentifiantSummaryItemToFill -> forAuthentifiant(item.primaryItem, spec)
        is CreditCardSummaryItemToFill -> forCreditCard(item.primaryItem, spec)
        is EmailItemToFill -> forEmail(item.primaryItem, spec)
        else -> null
    }

    override fun forLogout(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_logged_out_state_title))
        sliceBuilder.setStartIcon(iconDashlane)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forOnBoarding(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setStartIcon(iconOnBoarding)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_on_boarding_title))
        sliceBuilder.setSubtitle(applicationContext.resources.getString(R.string.inline_on_boarding_subtitle))
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forViewAllItems(spec: InlinePresentationSpec?): InlinePresentation? = createViewAllAccountsInline(spec)

    override fun forViewAllItemsOnEmptyResults(spec: InlinePresentationSpec?): InlinePresentation? =
        createViewAllAccountsInline(spec)

    
    override fun forPause(spec: InlinePresentationSpec?): InlinePresentation? = null

    override fun forAddAccount(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_add_new_account_title))
        sliceBuilder.setStartIcon(iconAddAccount)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forChangePassword(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_change_password_title))
        sliceBuilder.setStartIcon(iconChangePassword)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forSmsOtp(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_sms_otp_title))
        sliceBuilder.setStartIcon(iconOtp)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forPinnedItem(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setStartIcon(iconDashlane)
        return createInlinePresentation(inlineContent = sliceBuilder.build(), spec = spec, isPinned = true)
    }

    

    private fun forCreditCard(
        item: SummaryObject.PaymentCreditCard,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val title = inlineStringProvider.getCreditCardTitle(item)
        val subTitle = inlineStringProvider.getCreditCardSubtitle(item)
        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(title)
        sliceBuilder.setSubtitle(subTitle)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    

    private fun forAuthentifiant(
        item: SummaryObject.Authentifiant,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(inlineStringProvider.getAuthentifiantTitle(item))
        sliceBuilder.setSubtitle(inlineStringProvider.getAuthentifiantSubtitle(item))
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    

    private fun forEmail(
        item: SummaryObject.Email,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(inlineStringProvider.getEmailTitle(item))
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    

    private fun createViewAllAccountsInline(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_view_all_account_title))
        sliceBuilder.setStartIcon(iconViewAllAccount)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    

    private fun createInlinePresentation(
        inlineContent: InlineSuggestionUi.Content,
        spec: InlinePresentationSpec,
        isPinned: Boolean = false
    ): InlinePresentation {
        return InlinePresentation(inlineContent.slice, spec, isPinned)
    }

    private fun Icon.applyTint(): Icon = setTint(getColor(applicationContext, R.color.text_brand_standard))

    

    internal class InlineSuggestionStringProvider(private val context: Context) {

        fun getCreditCardTitle(item: SummaryObject.PaymentCreditCard) =
            item.cardNumberLastFourDigits?.let { "**** $it" } ?: ""

        fun getCreditCardSubtitle(item: SummaryObject.PaymentCreditCard) = item.name
            ?: item.creditCardTypeName
            ?: context.getString(R.string.inline_credit_card_fallback_title)

        fun getAuthentifiantTitle(item: SummaryObject.Authentifiant) =
            item.loginForUi ?: context.getString(R.string.inline_account_fallback_title)

        fun getAuthentifiantSubtitle(item: SummaryObject.Authentifiant) =
            item.titleForList ?: ""

        fun getEmailTitle(item: SummaryObject.Email) =
            item.email ?: context.getString(R.string.inline_email_fallback_title)
    }
}
