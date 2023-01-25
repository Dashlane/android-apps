package com.dashlane.navigation

import android.annotation.SuppressLint
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.xml.domain.SyncObjectType
import java.util.regex.Pattern



object SchemeUtils {
    private const val ITEM_ID_REGEX = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}"
    private val ITEM_ID_PATTERN = Pattern.compile(ITEM_ID_REGEX)

    private val HOME_FILTER = listOf(
        NavigationHelper.Destination.MainPath.PASSWORDS,
        NavigationHelper.Destination.MainPath.NOTES,
        NavigationHelper.Destination.MainPath.PAYMENTS,
        NavigationHelper.Destination.MainPath.PERSONAL_INFO,
        NavigationHelper.Destination.MainPath.ID_DOCUMENT
    )

    

    fun getItemId(uri: String?): String? {
        uri ?: return null
        val m = ITEM_ID_PATTERN.matcher(uri)
        return if (m.find()) {
            m.group(0)
        } else null
    }

    @JvmStatic
    fun getDataType(host: String?): SyncObjectType? {
        host ?: return null
        return when (host) {
            NavigationHelper.Destination.MainPath.ADDRESSES -> SyncObjectType.ADDRESS
            NavigationHelper.Destination.MainPath.BANK_ACCOUNTS -> SyncObjectType.BANK_STATEMENT
            NavigationHelper.Destination.MainPath.PASSWORDS -> SyncObjectType.AUTHENTIFIANT
            NavigationHelper.Destination.MainPath.COMPANIES -> SyncObjectType.COMPANY
            NavigationHelper.Destination.MainPath.DRIVER_LICENSES -> SyncObjectType.DRIVER_LICENCE
            NavigationHelper.Destination.MainPath.EMAILS -> SyncObjectType.EMAIL
            NavigationHelper.Destination.MainPath.FISCAL -> SyncObjectType.FISCAL_STATEMENT
            NavigationHelper.Destination.MainPath.ID_CARDS -> SyncObjectType.ID_CARD
            NavigationHelper.Destination.MainPath.IDENTITIES -> SyncObjectType.IDENTITY
            NavigationHelper.Destination.MainPath.PASSPORTS -> SyncObjectType.PASSPORT
            NavigationHelper.Destination.MainPath.CREDIT_CARDS -> SyncObjectType.PAYMENT_CREDIT_CARD
            NavigationHelper.Destination.MainPath.PAYPAL_ACCOUNTS -> SyncObjectType.PAYMENT_PAYPAL
            NavigationHelper.Destination.MainPath.PHONES -> SyncObjectType.PHONE
            NavigationHelper.Destination.MainPath.NOTES -> SyncObjectType.SECURE_NOTE
            NavigationHelper.Destination.MainPath.SOCIAL_SECURITY_NUMBERS -> SyncObjectType.SOCIAL_SECURITY_STATEMENT
            NavigationHelper.Destination.MainPath.WEBSITES -> SyncObjectType.PERSONAL_WEBSITE
            else -> null
        }
    }

    

    @JvmStatic
    @SuppressLint("SwitchIntDef")
    fun getHost(@DataIdentifierId.Def desktopIdentifier: Int): String {
        return when (desktopIdentifier) {
            DataIdentifierId.ADDRESS -> {
                NavigationHelper.Destination.MainPath.ADDRESSES
            }
            DataIdentifierId.BANK_STATEMENT -> {
                NavigationHelper.Destination.MainPath.BANK_ACCOUNTS
            }
            DataIdentifierId.AUTHENTIFIANT -> {
                NavigationHelper.Destination.MainPath.PASSWORDS
            }
            DataIdentifierId.COMPANY -> {
                NavigationHelper.Destination.MainPath.COMPANIES
            }
            DataIdentifierId.DRIVER_LICENCE -> {
                NavigationHelper.Destination.MainPath.DRIVER_LICENSES
            }
            DataIdentifierId.EMAIL -> {
                NavigationHelper.Destination.MainPath.EMAILS
            }
            DataIdentifierId.FISCAL_STATEMENT -> {
                NavigationHelper.Destination.MainPath.FISCAL
            }
            DataIdentifierId.ID_CARD -> {
                NavigationHelper.Destination.MainPath.ID_CARDS
            }
            DataIdentifierId.IDENTITY -> {
                NavigationHelper.Destination.MainPath.IDENTITIES
            }
            DataIdentifierId.PASSPORT -> {
                NavigationHelper.Destination.MainPath.PASSPORTS
            }
            DataIdentifierId.PAYMENT_CREDIT_CARD -> {
                NavigationHelper.Destination.MainPath.CREDIT_CARDS
            }
            DataIdentifierId.PAYMENT_PAYPAL -> {
                NavigationHelper.Destination.MainPath.PAYPAL_ACCOUNTS
            }
            DataIdentifierId.PHONE -> {
                NavigationHelper.Destination.MainPath.PHONES
            }
            DataIdentifierId.SECURE_NOTE -> {
                NavigationHelper.Destination.MainPath.NOTES
            }
            DataIdentifierId.SOCIAL_SECURITY_STATEMENT -> {
                NavigationHelper.Destination.MainPath.SOCIAL_SECURITY_NUMBERS
            }
            DataIdentifierId.PERSONAL_WEBSITE -> {
                NavigationHelper.Destination.MainPath.WEBSITES
            }
            
            else -> NavigationHelper.Destination.MainPath.ITEMS
        }
    }

    fun isHomeFilter(host: String): Boolean {
        return host in HOME_FILTER
    }
}