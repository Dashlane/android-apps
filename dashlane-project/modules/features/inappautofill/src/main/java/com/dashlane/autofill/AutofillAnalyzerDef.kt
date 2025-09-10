package com.dashlane.autofill

import android.content.Context
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import java.time.Month
import java.time.Year

interface AutofillAnalyzerDef {
    interface DatabaseAccess {
        fun clearCache()

        val isLoggedIn: Boolean

        fun loadAuthentifiantsByPackageName(packageName: String): List<SummaryObject.Authentifiant>?

        fun loadAuthentifiantsByUrl(url: String): List<SummaryObject.Authentifiant>?

        fun <T : SummaryObject> loadSummaries(type: SyncObjectType): List<T>?

        fun <T : SummaryObject> loadSummary(uid: String): T?

        suspend fun <T : SyncObject> loadSyncObject(itemId: String): VaultItem<T>?

        suspend fun createNewAuthentifiantFromAutofill(
            context: Context,
            title: String?,
            website: String?,
            login: String,
            password: String,
            packageName: String?,
            spaceId: String?,
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun saveAuthentifiant(
            context: Context,
            website: String?,
            packageName: String,
            login: String?,
            password: String?,
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun updateAuthentifiantWebsite(
            uid: String,
            website: String,
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun updateLastViewDate(
            itemId: String,
            instant: Instant,
        )

        suspend fun updateLastUsed(
            itemId: String,
            instant: Instant,
        )

        suspend fun saveCreditCard(
            number: String?,
            securityCode: String?,
            expireMonth: Month?,
            expireYear: Year?,
        ): VaultItem<SyncObject.PaymentCreditCard>?

        suspend fun updateAuthentifiantPassword(
            uid: String?,
            password: String?,
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun addAuthentifiantLinkedWebDomain(uid: String, website: String): Boolean

        suspend fun addAuthentifiantLinkedApp(uid: String, packageName: String): Boolean

        val authentifiantCount: Int
    }

    fun interface AutofillSecurityApplication {
        fun getSignatureVerification(
            context: Context,
            packageName: String,
            authentifiant: SummaryObject.Authentifiant,
        ): SignatureVerification
    }
}