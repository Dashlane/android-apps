package com.dashlane.ui.menu.item

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.canUpgradeToGetVpn

class VpnMenuItemPremiumTagProvider(
    private val userFeaturesChecker: UserFeaturesChecker
) : MenuItemPremiumTagProvider {
    constructor() : this(
        SingletonProvider.getUserFeatureChecker()
    )

    override val premiumTag: MenuItem.PremiumTag
        get() {
            val hasCapability = userFeaturesChecker.has(UserFeaturesChecker.Capability.VPN_ACCESS)
            val canUpgradeToGet = userFeaturesChecker.canUpgradeToGetVpn()

            return if (!hasCapability && canUpgradeToGet) {
                MenuItem.PremiumTag.PremiumOnly
            } else {
                MenuItem.PremiumTag.None
            }
        }
}