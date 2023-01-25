package com.dashlane.authenticator

import android.content.Context
import android.net.Uri
import android.os.Binder
import androidx.core.net.toUri
import com.dashlane.BuildConfig
import com.dashlane.authenticator.ipc.PasswordManagerAuthentifiant
import com.dashlane.authenticator.ipc.PasswordManagerService
import com.dashlane.authenticator.ipc.PasswordManagerState
import com.dashlane.authenticator.ipc.isPaired
import com.dashlane.core.sync.DataSyncHelper
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.LockValidator
import com.dashlane.network.tools.authorization
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.authenticator.SetAuthenticatorService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.credentialFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toHttpUrl
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.tryAsSuccess
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class PasswordManagerServiceStubImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val lockManager: LockManager,
    private val setAuthenticatorService: SetAuthenticatorService,
    private val mainDataAccessor: MainDataAccessor,
    private val urlDomainRegistryFactory: UrlDomainRegistryFactory,
    private val lockValidator: LockValidator,
    private val syncHelper: DataSyncHelper,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val isSettingUp2faChecker: IsSettingUp2faChecker,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider
) : PasswordManagerService.Stub(
    context = context,
    checkCallingSignature = BuildConfig.CHECK_AUTHENTICATOR_SIGNATURE
) {
    
    private val pairedCallingUids = CopyOnWriteArraySet<Int>()

    init {
        sessionManager.attach(object : SessionObserver {
            override suspend fun sessionEnded(
                session: Session,
                byUser: Boolean,
                forceLogout: Boolean
            ) {
                
                pairedCallingUids.clear()
            }
        })
    }

    override fun getState() = when {
        sessionManager.session == null -> {
            if (globalPreferencesManager.getDefaultUsername() != null) {
                PasswordManagerState.LOGGED_OUT
            } else {
                PasswordManagerState.NO_USER
            }
        }
        Binder.getCallingUid() in pairedCallingUids -> getLockTypeStatePaired()
        else -> getLockTypeStatePairable()
    }

    override fun pairWithBiometric(): String? {
        if (getState() != PasswordManagerState.PAIRABLE_BIOMETRIC) return null
        val userId = sessionManager.session?.userId ?: return null
        addPairedCallingUids()
        return userId
    }

    override fun pairWithPin(pin: String): String? {
        if (getState() != PasswordManagerState.PAIRABLE_PIN) return null
        val userId = sessionManager.session?.userId ?: return null
        return if (lockValidator.check(LockPass.ofPin(pin))) {
            addPairedCallingUids()
            userId
        } else {
            null
        }
    }

    override fun validatePin(pin: String): Boolean {
        return lockValidator.check(LockPass.ofPin(pin))
    }

    override fun pair(): String? {
        if (getState() != PasswordManagerState.PAIRABLE_BIOMETRIC &&
            getState() != PasswordManagerState.PAIRABLE_PIN
        ) return null

        val userId = sessionManager.session?.userId ?: return null

        val callingUid = Binder.getCallingUid()
        pairedCallingUids += callingUid

        return userId
    }

    override fun setPushId(userId: String, pushId: String): Boolean {
        if (!getState().isPaired()) return false

        val session = sessionManager.session?.takeIf { it.userId == userId } ?: return false

        if (pushId == userPreferencesManager.registeredAuthenticatorPushId) return true

        return runBlocking {
            tryAsSuccess {
                setAuthenticatorService.execute(
                    session.authorization,
                    SetAuthenticatorService.Request(
                        push = SetAuthenticatorService.Request.Push(
                            pushId = pushId,
                            platform = SetAuthenticatorService.Request.Push.Platform.GCM
                        )
                    )
                )

                userPreferencesManager.registeredAuthenticatorPushId = pushId
            }
        }
    }

    override fun getUserId(): String? {
        if (!getState().isPaired()) return null
        return sessionManager.session?.userId
    }

    override fun getDeviceAccessKey(userId: String): String? {
        if (!getState().isPaired()) return null
        return sessionManager.session?.takeIf { it.userId == userId }?.accessKey
    }

    override fun getAuthentifiants(
        hasOtp: Boolean,
        domain: String?
    ): List<PasswordManagerAuthentifiant> {
        if (!getState().isPaired()) return emptyList()

        val items = mainDataAccessor.getCredentialDataQuery().queryAll(
            credentialFilter {
                ignoreUserLock()
                if (domain != null) forDomain(domain)
            }
        ).run {
            if (hasOtp) filter { it.hasOtpUrl } else this
        }

        return mainDataAccessor.getVaultDataQuery().queryAll(
            vaultFilter {
                ignoreUserLock()
                specificUid(items.map { it.id })
            }
        ).map { (it as VaultItem<SyncObject.Authentifiant>).toPasswordManagerAuthentifiant() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveAuthentifiant(
        authentifiant: PasswordManagerAuthentifiant
    ): PasswordManagerAuthentifiant? {
        logD { "HasId=${authentifiant.id != null} HasOtpUrl=${authentifiant.otpUrl != null}" }

        if (!getState().isPaired()) return null

        logD { "Paired=${true}" }

        val vaultItem = if (authentifiant.id == null) {
            authentifiant.toVaultItem()
        } else {
            mainDataAccessor.getVaultDataQuery()
                .query(VaultFilter().apply {
                    ignoreUserLock()
                    specificDataType(SyncObjectType.AUTHENTIFIANT)
                    specificUid(authentifiant.id!!)
                    onlyShareable()
                })
                ?.let { it as VaultItem<SyncObject.Authentifiant> }
                ?.copyWithOtpUrl(authentifiant.otpUrl)
                ?.copySyncObject {
                    this.title = authentifiant.title
                    this.isFavorite = authentifiant.isFavorite
                    this.login = authentifiant.login
                    this.url = authentifiant.urlDomain
                    this.userSelectedUrl = authentifiant.urlDomain
                }
                ?: authentifiant.toVaultItem() 
        } ?: return null

        val saved = runBlocking { 
            mainDataAccessor.getDataSaver().save(vaultItem)
        }

        logD { "Item Saved=$saved" }

        return if (saved) {
            sessionManager.session?.let { session ->
                syncHelper.runSync(session, UsageLogCode134.Origin.SAVE)
            }
            vaultItem.toPasswordManagerAuthentifiant()
        } else {
            null
        }
    }

    override fun setAuthentifiantIsFavorite(authentifiant: PasswordManagerAuthentifiant): PasswordManagerAuthentifiant? {
        logD { "HasId=${authentifiant.id != null} HasOtpUrl=${authentifiant.otpUrl != null}" }
        if (!getState().isPaired() || authentifiant.id == null) return null
        logD { "Paired=${true}" }

        val vaultItem = mainDataAccessor.getVaultDataQuery()
            .query(VaultFilter().apply {
                ignoreUserLock()
                specificUid(authentifiant.id!!)
            })
            ?.let { it as VaultItem<SyncObject.Authentifiant> }
            ?.copySyncObject { this.isFavorite = authentifiant.isFavorite }
            ?: return null

        val saved = runBlocking { 
            mainDataAccessor.getDataSaver().save(vaultItem)
        }

        logD { "Item Saved=$saved" }

        return if (saved) {
            sessionManager.session?.let { session -> syncHelper.runSync(session, UsageLogCode134.Origin.SAVE) }
            vaultItem.toPasswordManagerAuthentifiant()
        } else null
    }

    override fun isSettingUp2fa() = isSettingUp2faChecker.isSettingUp2fa

    private fun addPairedCallingUids() {
        val callingUid = Binder.getCallingUid()
        pairedCallingUids += callingUid
    }

    private fun getLockTypeStatePaired(): PasswordManagerState {
        return when (lockManager.getLockType()) {
            LockTypeManager.LOCK_TYPE_BIOMETRIC -> PasswordManagerState.PAIRED_BIOMETRIC
            LockTypeManager.LOCK_TYPE_PIN_CODE -> PasswordManagerState.PAIRED_PIN
            else -> PasswordManagerState.NO_LOCK
        }
    }

    private fun getLockTypeStatePairable(): PasswordManagerState {
        return when (lockManager.getLockType()) {
            LockTypeManager.LOCK_TYPE_BIOMETRIC -> PasswordManagerState.PAIRABLE_BIOMETRIC
            LockTypeManager.LOCK_TYPE_PIN_CODE -> PasswordManagerState.PAIRABLE_PIN
            else -> PasswordManagerState.NO_LOCK
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun PasswordManagerAuthentifiant.toVaultItem() =
        otpUrl?.toUri()?.parseOtp()
            ?.let { otp ->
                val timestamp = Instant.now()

                val issuer = urlDomain ?: otp.issuer
                val login = login ?: otp.user

                createAuthentifiant(
                    dataIdentifier = CommonDataIdentifierAttrsImpl(
                        syncState = SyncState.MODIFIED,
                        creationDate = timestamp,
                        userModificationDate = timestamp
                    ),
                    title = SyncObject.Authentifiant.formatTitle(issuer),
                    deprecatedUrl = issuer?.let {
                        urlDomainRegistryFactory.create()
                            .search(it)
                            .firstOrNull()
                            ?.toHttpUrl()
                            ?.toString()
                    },
                    login = login,
                    otpSecret = otp.takeIf { it.isStandardOtp() }?.secret.toSyncObfuscatedValue(),
                    otpUrl = otp.url.toSyncObfuscatedValue(),
                    autoLogin = "true",
                    isFavorite = isFavorite
                )
            }.also { logD { "Item Created=${it != null}" } }

    private fun Uri.parseOtp() = UriParser.parse(this).also {
        logD { "Parse Success=${it != null}" }
    }

    private fun VaultItem<SyncObject.Authentifiant>.copyWithOtpUrl(url: String?) = copySyncObject {
        val otp = url?.toUri()?.parseOtp()

        val oldOtpUrl = otpUrl
        val oldOtpSecret = otpSecret

        otpUrl = otp?.url.toSyncObfuscatedValue()
        otpSecret = otp?.takeIf { it.isStandardOtp() }?.secret?.toSyncObfuscatedValue()

        logD { "Item Updated=${oldOtpUrl != otpUrl || oldOtpSecret != otpSecret}" }
    }.copyWithAttrs {
        syncState = SyncState.MODIFIED
        userModificationDate = Instant.now()
    }

    private inline fun logD(lazyMessage: () -> String) {
        val msg = "D/ [AuthenticatorIpc|Save] ${lazyMessage()}"
        userSupportFileLogger.add(msg)
    }

    private fun VaultItem<SyncObject.Authentifiant>.toPasswordManagerAuthentifiant() =
        PasswordManagerAuthentifiant(
            id = syncObject.id,
            title = syncObject.titleForListNormalized,
            login = syncObject.loginForUi,
            urlDomain = syncObject.urlDomain,
            otpUrl = syncObject.otpUrlCompat,
            editable = sharingPolicyDataProvider.canEditItem(toSummary(), false),
            isFavorite = syncObject.isFavorite == true
        )

    companion object {
        

        private val SyncObject.Authentifiant.otpUrlCompat: String?
            get() {
                val otpSecret = otpSecret?.takeUnless { it.isSemanticallyNull() }?.toString()
                val otpUrl = otpUrl?.takeUnless { it.isSemanticallyNull() }?.toString()

                return when {
                    otpSecret == null && otpUrl == null -> null
                    otpSecret == null && otpUrl != null -> otpUrl
                    otpSecret != null && otpUrl != null &&
                        otpUrl.toUri().getQueryParameter("secret") == otpSecret -> otpUrl
                    else -> {
                        val issuer = urlDomain.orEmpty()
                        val accountName = loginForUi
                        Uri.Builder()
                            .scheme("otpauth")
                            .authority("totp")
                            .appendPath(if (accountName != null) "$issuer:$accountName" else issuer)
                            .appendQueryParameter("secret", otpSecret.toString())
                            .appendQueryParameter("issuer", issuer)
                            .build()
                            .toString()
                    }
                }
            }
    }
}
