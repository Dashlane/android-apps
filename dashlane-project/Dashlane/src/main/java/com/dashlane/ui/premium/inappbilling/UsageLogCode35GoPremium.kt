package com.dashlane.ui.premium.inappbilling

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.navigation.NavigationHelper
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant

object UsageLogCode35GoPremium {

    @JvmStatic
    @JvmOverloads
    fun send(action: Int, subAction: String? = null) = send(action = action.toString(), subAction = subAction)

    @JvmStatic
    @JvmOverloads
    fun send(action: String, subAction: String? = null) { 
        SingletonProvider.getSessionManager().session
            ?.let { SingletonProvider.getComponent().bySessionUsageLogRepository[it] }
            ?.enqueue(
                UsageLogCode35(
                    type = UsageLogConstant.ViewType.goPremium,
                    action = action,
                    subaction = subAction
                ),
                true
            )
    }

    @JvmStatic
    fun isDeepLinkToPremium(deepLink: String?): Boolean {
        return (
            deepLink != null &&
                deepLink.contains(NavigationHelper.Destination.MainPath.GET_PREMIUM)
        )
    }
}