package com.dashlane.vault.util

import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.xml.domain.SyncObjectType

object SyncObjectTypeUtils {
    val ALL
        get() = arrayOf(
            SyncObjectType.ADDRESS,
            SyncObjectType.AUTH_CATEGORY,
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.COMPANY,
            SyncObjectType.DRIVER_LICENCE,
            SyncObjectType.EMAIL,
            SyncObjectType.FISCAL_STATEMENT,
            SyncObjectType.GENERATED_PASSWORD,
            SyncObjectType.ID_CARD,
            SyncObjectType.IDENTITY,
            SyncObjectType.PASSPORT,
            SyncObjectType.PAYMENT_PAYPAL,
            SyncObjectType.PAYMENT_CREDIT_CARD,
            SyncObjectType.PERSONAL_WEBSITE,
            SyncObjectType.PHONE,
            SyncObjectType.SOCIAL_SECURITY_STATEMENT,
            SyncObjectType.SECURE_NOTE,
            SyncObjectType.SECURE_NOTE_CATEGORY,
            SyncObjectType.BANK_STATEMENT,
            SyncObjectType.DATA_CHANGE_HISTORY,
            SyncObjectType.SECURE_FILE_INFO,
            SyncObjectType.SECURITY_BREACH
        )

    val WITH_TEAMSPACES
        get() = setOf(
            SyncObjectType.ADDRESS,
            SyncObjectType.AUTH_CATEGORY,
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.COMPANY,
            SyncObjectType.DRIVER_LICENCE,
            SyncObjectType.EMAIL,
            SyncObjectType.FISCAL_STATEMENT,
            SyncObjectType.GENERATED_PASSWORD,
            SyncObjectType.ID_CARD,
            SyncObjectType.IDENTITY,
            SyncObjectType.PASSPORT,
            SyncObjectType.PAYMENT_PAYPAL,
            SyncObjectType.PAYMENT_CREDIT_CARD,
            SyncObjectType.PERSONAL_WEBSITE,
            SyncObjectType.PHONE,
            SyncObjectType.SOCIAL_SECURITY_STATEMENT,
            SyncObjectType.SECURE_NOTE,
            SyncObjectType.SECURE_NOTE_CATEGORY,
            SyncObjectType.BANK_STATEMENT,
            SyncObjectType.SECURE_FILE_INFO
        )
    val SHAREABLE: Set<SyncObjectType>
        get() = setOf(
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.SECURE_NOTE
        )

    @JvmStatic
    fun getAll() = ALL

    @JvmStatic
    fun getWithTeamSpaces() = WITH_TEAMSPACES

    @JvmStatic
    fun getShareable() = SHAREABLE

    @JvmStatic
    fun valueFromDesktopId(@DataIdentifierId.Def id: Int): SyncObjectType {
        return valueFromDesktopIdIfExist(id)
            ?: throw IllegalArgumentException("No corresponding type for desktop id : $id")
    }

    @JvmStatic
    fun valueFromDesktopIdIfExist(@DataIdentifierId.Def id: Int): SyncObjectType? {
        return ALL.firstOrNull { it.desktopId == id }
    }
}
