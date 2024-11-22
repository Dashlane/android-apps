package com.dashlane.autofill.fillresponse

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.InlinePresentation
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
import com.dashlane.autofill.api.R
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.util.toBitmap
import com.dashlane.vault.util.BankDataProvider
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

    fun forOtp(spec: InlinePresentationSpec?): InlinePresentation?

    fun forPinnedItem(spec: InlinePresentationSpec?, phishingAttemptLevel: PhishingAttemptLevel): InlinePresentation?

    fun forPhishingWarning(
        spec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): InlinePresentation?
}

@RequiresApi(Build.VERSION_CODES.R)
internal class InlinePresentationProviderImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val bankDataProvider: BankDataProvider,
    navigationService: AutofillNavigationService
) : InlinePresentationProvider {

    private val longPressIntent = navigationService.getLongPressActionOnInline()

    private val iconViewAllAccount
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_action_search_outlined).applyTint()
    private val iconAddAccount
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_action_add_outlined).applyTint()
    private val iconChangePassword
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_action_refresh_outlined).applyTint()
    private val iconDashlane
        get() = Icon.createWithResource(applicationContext, R.drawable.vd_logo_dashlane_micro_logomark).applyTint()
    private val iconOtp
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_chat_outlined).applyTint()
    private val iconOnBoarding
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_feature_autofill_outlined).applyTint()
    private val iconLogin
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_item_login_outlined).applyTint()
    private val iconEmail
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_item_email_outlined).applyTint()
    private val creditCard
        get() = Icon.createWithResource(applicationContext, R.drawable.ic_item_payment_outlined).applyTint()

    override fun forItem(
        item: ItemToFill,
        spec: InlinePresentationSpec?
    ) = when (item) {
        is AuthentifiantItemToFill -> forAuthentifiant(item, spec)
        is CreditCardItemToFill -> forCreditCard(item, spec)
        is EmailItemToFill -> forEmail(item, spec)
        is OtpItemToFill -> forOtp(spec)
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

    override fun forOtp(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_sms_otp_title))
        sliceBuilder.setStartIcon(iconOtp)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    override fun forPinnedItem(
        spec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        val icon = when (phishingAttemptLevel) {
            PhishingAttemptLevel.NONE -> iconDashlane
            PhishingAttemptLevel.MODERATE -> buildColoredIcon(
                applicationContext,
                R.drawable.vd_logo_dashlane_micro_logomark,
                R.color.text_warning_standard,
            )
            PhishingAttemptLevel.HIGH -> buildColoredIcon(
                applicationContext,
                R.drawable.vd_logo_dashlane_micro_logomark,
                R.color.text_danger_standard,
            )
        }
        sliceBuilder.setStartIcon(icon)
        return createInlinePresentation(inlineContent = sliceBuilder.build(), spec = spec, isPinned = true)
    }

    private fun buildColoredIcon(context: Context, @DrawableRes drawableRes: Int, @ColorRes colorRes: Int) =
        AppCompatResources.getDrawable(context, drawableRes)
            ?.apply {
                DrawableCompat.wrap(this)
                DrawableCompat.setTint(this, getColor(context, colorRes))
            }
            ?.toBitmap()
            ?.run { Icon.createWithBitmap(this) }
            ?.apply { setTintBlendMode(BlendMode.DST) }
            ?: Icon.createWithResource(context, drawableRes)

    override fun forPhishingWarning(
        spec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        val text = when (phishingAttemptLevel) {
            PhishingAttemptLevel.MODERATE ->
                applicationContext.resources.getString(R.string.inline_phishing_moderate)
            PhishingAttemptLevel.HIGH ->
                applicationContext.resources.getString(R.string.inline_phishing_high)
            PhishingAttemptLevel.NONE -> throw IllegalStateException("PhishingAttemptLevel should not be NONE here")
        }
        val icon = when (phishingAttemptLevel) {
            PhishingAttemptLevel.MODERATE ->
                Icon.createWithResource(applicationContext, R.drawable.ic_inline_phishing_moderate).apply {
                    setTintBlendMode(BlendMode.DST)
                }
            PhishingAttemptLevel.HIGH ->
                Icon.createWithResource(applicationContext, R.drawable.ic_inline_phishing_high).apply {
                    setTintBlendMode(BlendMode.DST)
                }
            PhishingAttemptLevel.NONE -> throw IllegalStateException("PhishingAttemptLevel should not be NONE here")
        }
        sliceBuilder.setTitle(text)
        sliceBuilder.setStartIcon(icon)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    private fun forCreditCard(
        item: CreditCardItemToFill,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setStartIcon(creditCard)
        sliceBuilder.setTitle(item.cardNumberObfuscate)
        sliceBuilder.setSubtitle(
            item.name
                ?: item.bankName?.let {
                    bankDataProvider.getBankConfiguration(it)
                        .takeUnless { bankConfiguration ->
                            bankConfiguration == BankDataProvider.DEFAULT_BANK
                        }?.displayName
                }
                ?: applicationContext.getString(R.string.inline_credit_card_fallback_title)
        )
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    private fun forAuthentifiant(
        item: AuthentifiantItemToFill,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(item.login ?: applicationContext.getString(R.string.inline_account_fallback_title))
        sliceBuilder.setSubtitle(item.title ?: "")
        sliceBuilder.setStartIcon(iconLogin)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    private fun forEmail(
        item: EmailItemToFill,
        spec: InlinePresentationSpec?
    ): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(item.email ?: applicationContext.getString(R.string.inline_email_fallback_title))
        sliceBuilder.setStartIcon(iconEmail)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    private fun createViewAllAccountsInline(spec: InlinePresentationSpec?): InlinePresentation? {
        spec ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(longPressIntent)
        sliceBuilder.setTitle(applicationContext.resources.getString(R.string.inline_view_all_account_title))
        sliceBuilder.setStartIcon(iconViewAllAccount)
        return createInlinePresentation(sliceBuilder.build(), spec)
    }

    @SuppressLint("RestrictedApi")
    private fun createInlinePresentation(
        inlineContent: InlineSuggestionUi.Content,
        spec: InlinePresentationSpec,
        isPinned: Boolean = false
    ): InlinePresentation {
        return InlinePresentation(inlineContent.slice, spec, isPinned)
    }

    private fun Icon.applyTint(): Icon = setTint(getColor(applicationContext, R.color.text_brand_standard))
}
