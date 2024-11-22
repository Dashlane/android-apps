package com.dashlane.vpn.thirdparty.activate

import com.dashlane.session.authorization
import com.dashlane.server.api.endpoints.vpn.VpnGetCredentialsService
import com.dashlane.session.SessionManager
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import com.dashlane.vpn.thirdparty.VpnThirdPartyDataProvider
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.skocken.presentation.provider.BaseDataProvider

class VpnThirdPartyActivateAccountDataProvider(
    private val getVpnCredentialsService: VpnGetCredentialsService,
    private val sessionManager: SessionManager,
    private val authentifiantHelper: VpnThirdPartyAuthentifiantHelper
) : BaseDataProvider<VpnThirdPartyActivateAccountContract.Presenter>(),
    VpnThirdPartyActivateAccountContract.DataProvider {

    override suspend fun createHotspotShieldAccount(email: String) =
        getVpnCredentialsService.execute(
            sessionManager.session!!.authorization,
            VpnGetCredentialsService.Request(email)
        )

    override suspend fun saveAccount(title: String, email: String, password: String) {
        authentifiantHelper.addAuthentifiant(
            authentifiantHelper.newAuthentifiant(
                title = title,
                deprecatedUrl = VpnThirdPartyDataProvider.HOTSPOT_SHIELD_DOMAIN,
                email = email,
                password = SyncObfuscatedValue(password)
            )
        )
    }
}