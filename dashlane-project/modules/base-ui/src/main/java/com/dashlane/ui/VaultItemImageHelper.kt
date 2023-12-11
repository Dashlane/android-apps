package com.dashlane.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.dashlane.util.getPlaceholder
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.graphics.CredentialRemoteDrawable
import com.dashlane.util.graphics.PasskeyRemoteDrawable
import com.dashlane.util.graphics.RoundRectDrawable
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object VaultItemImageHelper {
    fun getIconDrawableFromSyncObject(
        context: Context,
        syncObject: SyncObject
    ): RoundRectDrawable? {
        return getIconDrawableFromSummaryObject(context, syncObject.toSummary())
    }

    fun getIconDrawableFromSummaryObject(
        context: Context,
        summaryObject: SummaryObject,
    ): RoundRectDrawable? {
        return when (summaryObject) {
            is SummaryObject.Authentifiant -> {
                val url = summaryObject.navigationUrl ?: summaryObject.url
                getAuthentifiantIcon(context, url, summaryObject.title)
            }
            is SummaryObject.Passkey -> {
                getPasskeyIcon(context, summaryObject.rpId, summaryObject.rpId)
            }
            is SummaryObject.SecureNote ->
                getSecureNoteIcon(
                    context,
                    summaryObject.type ?: SyncObject.SecureNoteType.NO_TYPE
                )
            is SummaryObject.PaymentCreditCard -> getCreditCardIcon(context, summaryObject.color)
            else -> getOtherTypeIcon(context, summaryObject.syncObjectType)
        }
    }

    fun getCreditCardIcon(
        context: Context,
        color: SyncObject.PaymentCreditCard.Color?
    ): RoundRectDrawable {
        val colorRes = ContextCompat.getColor(context, color.getColorResource())
        return createRoundedImage(
            context,
            context.getThemeAttrColor(R.attr.colorPrimary),
            R.drawable.ico_list_card
        ).apply {
            backgroundColor = colorRes
        }
    }

    private fun getSecureNoteIcon(
        context: Context,
        secureNoteType: SyncObject.SecureNoteType
    ): RoundRectDrawable? {
        val secureNoteColor = ContextCompat.getColor(context, secureNoteType.getColorId())
        return RoundRectDrawable.newWithImage(
            context,
            secureNoteColor,
            R.drawable.ic_list_secure_note
        )
    }

    @Suppress("LongMethod")
    private fun getOtherTypeIcon(context: Context, syncObjectType: SyncObjectType): RoundRectDrawable? {
        return when (syncObjectType) {
            SyncObjectType.ADDRESS -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_address
            )
            SyncObjectType.BANK_STATEMENT -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_bank_account
            )
            SyncObjectType.COMPANY -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_company
            )
            SyncObjectType.DRIVER_LICENCE -> createRoundedImage(
                context,
                ContextCompat.getColor(context, R.color.ico_list_driving_licence),
                R.drawable.ico_list_driving_licence
            )
            SyncObjectType.EMAIL -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_mail
            )
            SyncObjectType.FISCAL_STATEMENT -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_tax_number
            )
            SyncObjectType.ID_CARD -> createRoundedImage(
                context,
                ContextCompat.getColor(context, R.color.ico_list_id_card),
                R.drawable.ico_list_id_card
            )
            SyncObjectType.IDENTITY -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_name
            )
            SyncObjectType.PASSPORT -> createRoundedImage(
                context,
                ContextCompat.getColor(context, R.color.ico_list_passport),
                R.drawable.ico_list_passport
            )
            SyncObjectType.PAYMENT_PAYPAL -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_paypal
            )
            SyncObjectType.PERSONAL_WEBSITE -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_website
            )
            SyncObjectType.PHONE -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_phone
            )
            SyncObjectType.SOCIAL_SECURITY_STATEMENT -> createRoundedImage(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary),
                R.drawable.ico_list_social_security
            )
            else -> null
        }
    }

    fun getAuthentifiantIcon(context: Context, url: String?, title: String?): RoundRectDrawable {
        val urlIconDrawable = CredentialRemoteDrawable(
            context,
            context.getThemeAttrColor(R.attr.colorPrimary)
        ).apply {
            preferImageBackgroundColor = true
        }
        val placeholder = context.getPlaceholder(title)
        urlIconDrawable.loadImage(url, placeholder)
        return urlIconDrawable
    }

    private fun getPasskeyIcon(context: Context, url: String?, title: String?): RoundRectDrawable {
        val urlIconDrawable = PasskeyRemoteDrawable(
            context,
            context.getThemeAttrColor(R.attr.colorPrimary)
        )
        val placeholder = context.getPlaceholder(title)
        urlIconDrawable.loadImage(url, placeholder)
        return urlIconDrawable
    }

    private fun createRoundedImage(context: Context, color: Int, icon: Int): RoundRectDrawable {
        return RoundRectDrawable.newWithImage(context, color, icon).apply {
            preferImageBackgroundColor = true
        }
    }
}