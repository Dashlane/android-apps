package com.dashlane.vpn.thirdparty

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import androidx.core.net.toUri
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.credentialFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.getPackageInfoCompat
import com.dashlane.util.resolveActivityCompat
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vpn.thirdparty.VpnThirdPartyContract.DataProvider.Account
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.provider.BaseDataProvider
import java.time.Instant

class VpnThirdPartyDataProvider(
    private val packageManager: PackageManager,
    private val credentialDataQuery: CredentialDataQuery,
    private val vaultDataQuery: VaultDataQuery
) : BaseDataProvider<VpnThirdPartyContract.Presenter>(), VpnThirdPartyContract.DataProvider {

    override val isHotspotShieldInstalled: Boolean
        get() = try {
            packageManager.getPackageInfoCompat(HOTSPOT_SHIELD_PACKAGE_NAME, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    private val deepLinkIntent = Intent(ACTION_VIEW).apply {
        data = SIGN_IN_URL.toUri()
        setPackage(HOTSPOT_SHIELD_PACKAGE_NAME)
    }

    override val signInIntent: Intent
        get() = if (packageManager.resolveActivityCompat(deepLinkIntent, MATCH_DEFAULT_ONLY) != null) {
            
            deepLinkIntent
        } else {
            
            packageManager.getLaunchIntentForPackage(HOTSPOT_SHIELD_PACKAGE_NAME)!!
        }

    override val installIntent = Intent(ACTION_VIEW).apply { data = INSTALL_URL.toUri() }

    override suspend fun getHotspotShieldAccount(): Account? {
        val accountsByDomain = credentialDataQuery.queryAll(credentialFilter {
            forDomain(HOTSPOT_SHIELD_DOMAIN)
        })
        val accountsByPackageName = credentialDataQuery.queryAll(credentialFilter {
            packageName = HOTSPOT_SHIELD_PACKAGE_NAME
        })
        val latestAccount = (accountsByDomain + accountsByPackageName).filter {
            it.urlForGoToWebsite?.contains(HOTSPOT_SHIELD_DOMAIN) == true
        }.maxByOrNull { it.creationDatetime ?: Instant.EPOCH } ?: return null
        val login = latestAccount.loginForUi ?: return null
        val password = (vaultDataQuery.query(vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(latestAccount.id)
        })?.syncObject as? SyncObject.Authentifiant)?.password?.toString() ?: return null
        return Account(
            login,
            password,
            latestAccount.id,
            latestAccount.urlForUsageLog
        )
    }

    companion object {
        private const val HOTSPOT_SHIELD_PACKAGE_NAME = "hotspotshield.android.vpn"
        private const val SIGN_IN_URL = "https://hotspotshield.aura.com/sign-in"
        private const val INSTALL_URL = "https://hsshield.page.link/sign-in"
        const val HOTSPOT_SHIELD_DOMAIN = "dashlane.hotspotshield.com"
    }
}