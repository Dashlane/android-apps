package com.dashlane.autofill.core

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.DataSync
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.counterFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.url.root
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.PackageUtilities
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.isValidEmail
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.valueWithoutWww
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createPaymentCreditCard
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.vault.util.copyWithNewPassword
import com.dashlane.vault.util.copyWithNewUrl
import com.dashlane.vault.util.getSignatureVerificationWith
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import java.time.Month
import java.time.Year
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Suppress("LargeClass")
class AutoFillDataBaseAccess @Inject constructor(
    private val sessionManager: SessionManager,
    private val mainDataAccessor: MainDataAccessor,
    @IoCoroutineDispatcher private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val dataSync: DataSync,
    private val knownApplicationProvider: KnownApplicationProvider,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) : AutofillAnalyzerDef.DatabaseAccess {

    private val dataCounter: DataCounter
        get() = mainDataAccessor.getDataCounter()
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    private var lastResult: LastResult? = null

    override val isLoggedIn: Boolean
        get() = sessionManager.session != null

    override fun <T : SummaryObject> loadSummary(uid: String): T? {
        return genericDataQuery.queryFirst(GenericFilter(uid)) as? T
    }

    override fun loadAuthentifiantsByPackageName(packageName: String): List<SummaryObject.Authentifiant>? {
        if (!isLoggedIn) return null
        val authentifiants = getCachedResult(packageName)
        if (authentifiants != null) {
            return authentifiants
        }

        val packageNameCredentials = loadAuthentifiantsForPackageName(packageName)
        val packageNameDomainsCredentials = loadAuthentifiantsPackageNameWhiteListDomains(packageName)?.let {
            it - packageNameCredentials
        } ?: emptyList()

        
        val verificationStatusToCredentials = (packageNameCredentials + packageNameDomainsCredentials)
            .groupBy { it.getSignatureVerificationWith(packageNameSignatureHelper, packageName) }

        val verified = verificationStatusToCredentials[PackageSignatureStatus.VERIFIED].orEmpty()
        val unknown = verificationStatusToCredentials[PackageSignatureStatus.UNKNOWN].orEmpty()

        val credentials = verified + unknown

        setCachedResult(packageName, credentials)
        return credentials
    }

    private fun loadAuthentifiantsForPackageName(packageName: String): List<SummaryObject.Authentifiant> {
        val dataQuery = mainDataAccessor.getCredentialDataQuery()
        val filter = dataQuery.createFilter().also { it.ignoreUserLock() }
        filter.packageName = packageName
        filter.allowSimilarDomains = true

        
        return dataQuery.queryAll(filter)
    }

    private fun loadAuthentifiantsPackageNameWhiteListDomains(packageName: String): List<SummaryObject.Authentifiant>? {
        val domain = knownApplicationProvider.getWhitelistedApplication(packageName)
            ?.mainDomain?.toUrlOrNull()?.root ?: return null
        val authentifiants = getCachedResult(domain)
        if (authentifiants != null) {
            return authentifiants
        }
        val dataQuery = mainDataAccessor.getCredentialDataQuery()
        val filter = dataQuery.createFilter().apply {
            ignoreUserLock()
            forDomain(domain)
            allowSimilarDomains = true
        }
        val credentials = dataQuery.queryAll(filter)
        setCachedResult(domain, credentials)
        return credentials
    }

    override fun loadAuthentifiantsByUrl(url: String): List<SummaryObject.Authentifiant>? {
        if (!isLoggedIn) return null
        val domain = url.toUrlOrNull()?.root ?: return null
        val authentifiants = getCachedResult(domain)
        if (authentifiants != null) {
            return authentifiants
        }
        val dataQuery = mainDataAccessor.getCredentialDataQuery()
        val filter = dataQuery.createFilter().apply {
            ignoreUserLock()
            forDomain(domain)
            allowSimilarDomains = true
        }
        val credentials = dataQuery.queryAll(filter)
        setCachedResult(domain, credentials)
        return credentials
    }

    override fun <T : SummaryObject> loadSummaries(type: SyncObjectType): List<T>? {
        if (!isLoggedIn) return null
        val filter = genericFilter {
            ignoreUserLock()
            specificDataType(type)
        }
        return genericDataQuery.queryAll(filter).mapNotNull { it as? T }
    }

    override fun <T : SyncObject> loadSyncObject(itemId: String): VaultItem<T>? {
        if (!isLoggedIn) return null
        val dataQuery = mainDataAccessor.getVaultDataQuery()
        val filter = vaultFilter {
            ignoreUserLock()
            specificUid(itemId)
        }
        return dataQuery.query(filter) as? VaultItem<T>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun createNewAuthentifiantFromAutofill(
        context: Context,
        title: String?,
        website: String?,
        login: String,
        password: String,
        packageName: String?,
        spaceId: String?
    ): VaultItem<SyncObject.Authentifiant>? {
        if (!isLoggedIn) return null

        val createTimestamp = Instant.now()
        val linkedServices = if (packageName != null) {
            linkedServicesHelper.getLinkedServicesWithAppSignature(packageName)
        } else {
            null
        }
        val obfuscatedPassword = SyncObfuscatedValue(password)
        val (newLogin, newEmail) = getLoginAndEmail(login)

        val newCredential = createAuthentifiant(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                syncState = SyncState.MODIFIED,
                creationDate = createTimestamp,
                userModificationDate = createTimestamp,
                teamSpaceId = spaceId
            ),
            title = title,
            deprecatedUrl = website,
            email = newEmail,
            login = newLogin,
            password = obfuscatedPassword,
            autoLogin = "true",
            passwordModificationDate = createTimestamp,
            linkedServices = linkedServices
        ).copyWithDefaultValue(context, sessionManager.session) as VaultItem<SyncObject.Authentifiant>
        if (!mainDataAccessor.getDataSaver().save(newCredential)) {
            
            return null
        }
        return newCredential
    }

    override suspend fun saveAuthentifiant(
        context: Context,
        website: String?,
        packageName: String,
        login: String?,
        password: String?
    ): VaultItem<SyncObject.Authentifiant>? {
        if (!isLoggedIn) return null

        val authentifiant = prepareAuthentifiantToSave(website, packageName, context, password, login)
        if (!mainDataAccessor.getDataSaver().save(authentifiant)) {
            
            return null
        }
        return authentifiant.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun updateAuthentifiantWebsite(
        uid: String,
        website: String
    ): VaultItem<SyncObject.Authentifiant>? {
        if (!isLoggedIn) return null

        val filter = vaultFilter {
            specificUid(uid)
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
        val account =
            mainDataAccessor.getVaultDataQuery().query(filter) as VaultItem<SyncObject.Authentifiant>? ?: return null
        val updatedAccount = account.copyWithNewUrl(website).copyWithAttrs { syncState = SyncState.MODIFIED }
        mainDataAccessor.getDataSaver().save(updatedAccount)
        return updatedAccount
    }

    override suspend fun updateLastViewDate(
        itemId: String,
        instant: Instant
    ) {
        if (!isLoggedIn) return

        val filter = vaultFilter {
            specificUid(itemId)
        }
        val updatedAccount =
            mainDataAccessor.getVaultDataQuery().query(filter)?.copyWithAttrs { locallyViewedDate = instant } ?: return
        mainDataAccessor.getDataSaver().save(updatedAccount)
    }

    private fun prepareAuthentifiantToSave(
        website: String?,
        packageName: String,
        context: Context,
        password: String?,
        login: String?
    ): VaultItem<*> {
        val createTimestamp = Instant.now()
        val (linkedServices, deprecatedUrl, newTitle) = getAuthentifiantData(website, packageName, context)

        val obfuscatedPassword = SyncObfuscatedValue(password ?: "")
        val (newLogin, newEmail) = getLoginAndEmail(login)

        return createAuthentifiant(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                syncState = SyncState.MODIFIED,
                creationDate = createTimestamp,
                userModificationDate = createTimestamp
            ),
            title = newTitle,
            deprecatedUrl = deprecatedUrl,
            email = newEmail,
            login = newLogin,
            password = obfuscatedPassword,
            autoLogin = "true",
            passwordModificationDate = createTimestamp,
            linkedServices = linkedServices
        ).copyWithDefaultValue(context, sessionManager.session)
    }

    private fun getLoginAndEmail(login: String?): Pair<String?, String?> {
        return if (login != null) {
            if (login.isValidEmail()) {
                null to login
            } else {
                login to null
            }
        } else {
            "" to ""
        }
    }

    private fun getAuthentifiantData(
        website: String?,
        packageName: String,
        context: Context
    ): Triple<SyncObject.Authentifiant.LinkedServices?, String?, String?> {
        val newTitle: String?
        val deprecatedUrl: String?
        val linkedServices: SyncObject.Authentifiant.LinkedServices?
        if (website != null) {
            
            newTitle = SyncObject.Authentifiant.formatTitle(website)
            deprecatedUrl = website
            linkedServices = null
        } else {
            
            linkedServices = linkedServicesHelper.getLinkedServicesWithAppSignature(packageName)

            
            val appWebsite = knownApplicationProvider.getKnownApplication(packageName)?.mainDomain
            newTitle = SyncObject.Authentifiant.formatTitle(
                appWebsite ?: PackageUtilities.getApplicationNameFromPackage(context, packageName)
            )
            deprecatedUrl = appWebsite
        }
        return Triple(linkedServices, deprecatedUrl, newTitle)
    }

    override suspend fun saveCreditCard(
        number: String?,
        securityCode: String?,
        expireMonth: Month?,
        expireYear: Year?
    ): VaultItem<SyncObject.PaymentCreditCard>? {
        if (!isLoggedIn) return null
        val creditCard = createPaymentCreditCard(
            dataIdentifier = CommonDataIdentifierAttrsImpl(syncState = SyncState.MODIFIED),
            cardNumber = number.toSyncObfuscatedValue(),
            securityCode = securityCode,
            expireMonth = expireMonth,
            expireYear = expireYear
        )
        if (mainDataAccessor.getDataSaver().save(creditCard)) {
            return creditCard
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun updateAuthentifiantPassword(
        uid: String?,
        password: String?
    ): VaultItem<SyncObject.Authentifiant>? {
        if (!isLoggedIn || uid == null || password == null) return null

        val filter = vaultFilter {
            specificUid(uid)
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
        val account =
            mainDataAccessor.getVaultDataQuery().query(filter) as VaultItem<SyncObject.Authentifiant>? ?: return null
        val updatedAccount = account.copyWithNewPassword(password).copyWithAttrs { syncState = SyncState.MODIFIED }
        mainDataAccessor.getDataSaver().save(updatedAccount)
        return updatedAccount
    }

    override suspend fun addAuthentifiantLinkedWebDomain(uid: String, website: String): Boolean {
        return withContext(ioCoroutineDispatcher) {
            loadSyncObject<SyncObject.Authentifiant>(uid)?.let { vault ->
                val newLinkedServices = linkedServicesHelper.addLinkedDomains(
                    vault.syncObject.linkedServices,
                    listOf(
                        SyncObject.Authentifiant.LinkedServices.AssociatedDomains(
                            website.toUrlDomainOrNull()?.valueWithoutWww(),
                            SyncObject.Authentifiant.LinkedServices.AssociatedDomains.Source.REMEMBER
                        )
                    )
                )
                val toSaveItem = vault.copySyncObject {
                    linkedServices = newLinkedServices
                }.copyWithAttrs {
                    userModificationDate = Instant.now()
                    syncState = SyncState.MODIFIED
                }
                return@withContext mainDataAccessor.getDataSaver().save(toSaveItem).also {
                    
                    teamspaceForceCategorizationManager.executeSync()

                    
                    dataSync.sync(Trigger.SAVE)
                }
            }
            return@withContext false
        }
    }

    override suspend fun addAuthentifiantLinkedApp(uid: String, packageName: String): Boolean {
        linkedServicesHelper.getLinkedServicesWithAppSignature(packageName).let {
            return withContext(ioCoroutineDispatcher) {
                loadSyncObject<SyncObject.Authentifiant>(uid)?.let { vault ->
                    val newLinkedServices = linkedServicesHelper.addLinkedApps(
                        vault.syncObject.linkedServices,
                        it.associatedAndroidApps ?: emptyList()
                    )
                    val toSaveItem = vault.copySyncObject {
                        linkedServices = newLinkedServices
                    }.copyWithAttrs {
                        userModificationDate = Instant.now()
                        syncState = SyncState.MODIFIED
                    }
                    return@withContext mainDataAccessor.getDataSaver().save(toSaveItem).also {
                        
                        dataSync.sync(Trigger.SAVE)
                    }
                }
                return@withContext false
            }
        }
    }

    override fun clearCache() {
        lastResult = null
    }

    override val authentifiantCount: Int
        get() = dataCounter.count(
            counterFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.AUTHENTIFIANT)
            }
        )

    private fun getCachedResult(query: String): List<SummaryObject.Authentifiant>? {
        return lastResult?.takeIf { it.query == query }?.authentifiants
    }

    private fun setCachedResult(query: String, authentifiants: List<SummaryObject.Authentifiant>) {
        lastResult = LastResult(query, authentifiants)
    }

    private data class LastResult(val query: String, val authentifiants: List<SummaryObject.Authentifiant>)
}