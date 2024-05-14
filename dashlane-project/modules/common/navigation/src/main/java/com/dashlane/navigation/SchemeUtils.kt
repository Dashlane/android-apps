package com.dashlane.navigation

import com.dashlane.xml.domain.SyncObjectType

object SchemeUtils {

    private val HOME_FILTER = listOf(
        NavigationHelper.Destination.MainPath.PASSWORDS,
        NavigationHelper.Destination.MainPath.NOTES,
        NavigationHelper.Destination.MainPath.PAYMENTS,
        NavigationHelper.Destination.MainPath.PERSONAL_INFO,
        NavigationHelper.Destination.MainPath.ID_DOCUMENT
    )

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

    fun isHomeFilter(host: String): Boolean {
        return host in HOME_FILTER
    }
}