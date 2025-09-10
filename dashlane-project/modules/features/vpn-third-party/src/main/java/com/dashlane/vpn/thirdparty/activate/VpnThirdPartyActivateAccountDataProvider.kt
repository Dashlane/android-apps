package com.dashlane.vpn.thirdparty.activate

import com.dashlane.server.api.endpoints.vpn.GetCredentialsService
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import com.dashlane.vpn.thirdparty.VpnThirdPartyRepository
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.skocken.presentation.provider.BaseDataProvider

class VpnThirdPartyActivateAccountDataProvider(
    private val getCredentialsService: GetCredentialsService,
    private val sessionManager: SessionManager,
    private val authentifiantHelper: VpnThirdPartyAuthentifiantHelper,
) : BaseDataProvider<VpnThirdPartyActivateAccountContract.Presenter>(),
    VpnThirdPartyActivateAccountContract.DataProvider {
    override suspend fun createHotspotShieldAccount(email: String) =
        getCredentialsService.execute(
            sessionManager.session!!.authorization,
            GetCredentialsService.Request(email),
        )

    override suspend fun saveAccount(title: String, email: String, password: String) {
        authentifiantHelper.addAuthentifiant(
            authentifiantHelper.newAuthentifiant(
                title = title,
                deprecatedUrl = VpnThirdPartyRepository.HOTSPOT_SHIELD_DOMAIN,
                email = email,
                password = SyncObfuscatedValue(password),
            ),
        )
    }
}