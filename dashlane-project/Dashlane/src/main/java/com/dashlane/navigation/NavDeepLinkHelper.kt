package com.dashlane.navigation

import android.content.Intent
import android.net.Uri
import com.dashlane.navigation.NavigationHelper.Destination.MainPath.AUTHENTICATOR_TOOL
import com.dashlane.navigation.NavigationHelper.Destination.MainPath.DARK_WEB_MONITORING
import com.dashlane.navigation.NavigationHelper.Destination.MainPath.DARK_WEB_MONITORING_PREMIUM_PROMPT
import com.dashlane.navigation.NavigationHelper.Destination.MainPath.GET_PREMIUM
import com.dashlane.navigation.NavigationHelper.Destination.MainPath.VPN
import com.dashlane.navigation.NavigationHelper.Destination.SecondaryPath
import com.dashlane.navigation.NavigationHelper.Destination.SecondaryPath.GetPremium
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.paywall.common.PaywallIntroType
import com.dashlane.util.logE
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType



class NavDeepLinkHelper(
    private val navigator: Navigator
) {

    fun overrideDeepLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        if (overrideOtpDeepLink(uri)) {
            return true
        }
        
        
        val pathSegmentWithHost = uri.pathSegments
        if (pathSegmentWithHost.isEmpty()) return false
        val host = pathSegmentWithHost[0]
        
        val pathSegments: List<String> = if (pathSegmentWithHost.size > 1) {
            pathSegmentWithHost.subList(1, pathSegmentWithHost.size)
        } else {
            emptyList()
        }
        val dataType = SchemeUtils.getDataType(host)
        val isHomeFilter = pathSegments.isEmpty() && SchemeUtils.isHomeFilter(host)

        val origin = uri.getQueryParameter("origin")
        return when {
            dataType != null && pathSegments.isNotEmpty() -> {
                
                handleItemDeepLink(dataType, pathSegments, origin)
                true
            }
            isHomeFilter -> {
                
                navigator.goToHome(origin, host)
                true
            }
            host == GET_PREMIUM -> {
                handleGetPremiumDeeplink(pathSegments, origin)
                true
            }
            host == VPN -> {
                navigator.goToVpn(origin)
                true
            }
            host == DARK_WEB_MONITORING -> {
                
                navigator.goToDarkWebMonitoring(origin)
                true
            }
            host == DARK_WEB_MONITORING_PREMIUM_PROMPT -> {
                navigator.goToPaywall(
                    type = PaywallIntroType.DARK_WEB_MONITORING.toString(),
                    origin = origin
                )
                true
            }
            host == AUTHENTICATOR_TOOL -> {
                navigator.goToAuthenticator()
                true
            }
            else -> false
        }
    }

    private fun handleItemDeepLink(
        dataType: SyncObjectType,
        pathSegments: List<String>,
        origin: String?
    ) {
        
        val uid = pathSegments.firstOrNull {
            Regex(ITEM_UID_REGEX).containsMatchIn(it)
        }?.let {
            if (!it.startsWith("{")) {
                return@let "{$it}"
            }
            return@let it
        }
        val path = pathSegments.singleOrNull {
            it == SecondaryPath.Items.NEW || it == SecondaryPath.Items.SHARE || it == SecondaryPath.Items.SHARE_INFO
        }
        when {
            path == SecondaryPath.Items.NEW -> {
                when (dataType) {
                    SyncObjectType.AUTHENTIFIANT -> {
                        navigator.goToCredentialAddStep1(origin)
                    }
                    else -> {
                        navigator.goToCreateItem(dataType.desktopId)
                    }
                }
            }
            (path == SecondaryPath.Items.SHARE || path == SecondaryPath.Items.SHARE_INFO) && uid != null ->
                handleSharingItemDeepLink(uid, path, dataType, origin)
            else -> {
                if (uid == null) {
                    logE { "Deeplink to item don't have any id provided" }
                } else {
                    navigator.goToItem(uid, dataType.desktopId)
                }
            }
        }
    }

    private fun handleSharingItemDeepLink(
        uid: String,
        subOperation: String,
        dataType: SyncObjectType,
        origin: String?
    ) {
        when (subOperation) {
            SecondaryPath.Items.SHARE -> {
                if (dataType == SyncObjectType.AUTHENTIFIANT) {
                    navigator.goToSharePeopleSelection(selectedPasswords = arrayOf(uid), origin = origin)
                } else if (dataType == SyncObjectType.SECURE_NOTE) {
                    navigator.goToSharePeopleSelection(selectedNotes = arrayOf(uid), origin = origin)
                }
            }
            SecondaryPath.Items.SHARE_INFO -> navigator.goToShareUsersForItems(uid)
        }
    }

    private fun handleGetPremiumDeeplink(pathSegments: List<String>, origin: String?) {
        val offerType = when {
            pathSegments.contains(GetPremium.ESSENTIALS_OFFER) -> OfferType.ADVANCED
            pathSegments.contains(GetPremium.PREMIUM_OFFER) -> OfferType.PREMIUM
            pathSegments.contains(GetPremium.FAMILY_OFFER) -> OfferType.FAMILY
            else -> null
        }
        navigator.goToOffers(origin, offerType?.toString())
    }

    private fun overrideOtpDeepLink(uri: Uri): Boolean {
        if (!uri.toString().startsWith("otpauth")) return false
        navigator.goToAuthenticator(uri)
        return true
    }

    companion object {
        private const val ITEM_UID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    }
}