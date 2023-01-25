package com.dashlane.vpn.thirdparty

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.ActivateVpnError.EMAIL_ALREADY_IN_USE
import com.dashlane.hermes.generated.definitions.ActivateVpnError.SERVER_ERROR
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.FlowStep.COMPLETE
import com.dashlane.hermes.generated.definitions.FlowStep.ERROR
import com.dashlane.hermes.generated.definitions.FlowStep.START
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.CopyVaultItemFieldAnonymous
import com.dashlane.hermes.generated.events.user.ActivateVpn
import com.dashlane.hermes.generated.events.user.CopyVaultItemField
import com.dashlane.hermes.generated.events.user.DownloadVpnClient
import javax.inject.Inject

class VpnThirdPartyLogger @Inject constructor(private val hermesLogRepository: LogRepository) {

    fun logViewPaywall() =
        hermesLogRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.PAYWALL_VPN)

    fun logClickActivateAccount() = hermesLogRepository.queueEvent(ActivateVpn(flowStep = START))

    fun logEmailInUseActivationError() =
        hermesLogRepository.queueEvent(ActivateVpn(EMAIL_ALREADY_IN_USE, ERROR))

    fun logServerActivationError() =
        hermesLogRepository.queueEvent(ActivateVpn(SERVER_ERROR, ERROR))

    fun logActivated() = hermesLogRepository.queueEvent(ActivateVpn(flowStep = COMPLETE))

    fun logCopyEmail(itemId: String, domain: String) {
        hermesLogRepository.queueEvent(
            CopyVaultItemField(
                field = Field.EMAIL,
                itemId = ItemId(id = itemId),
                itemType = ItemType.CREDENTIAL,
                isProtected = false
            )
        )
        hermesLogRepository.queueEvent(
            CopyVaultItemFieldAnonymous(
                field = Field.EMAIL,
                itemType = ItemType.CREDENTIAL,
                domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
            )
        )
    }

    fun logCopyPassword(itemId: String, domain: String) {
        hermesLogRepository.queueEvent(
            CopyVaultItemField(
                field = Field.PASSWORD,
                itemId = ItemId(id = itemId),
                itemType = ItemType.CREDENTIAL,
                isProtected = false
            )
        )
        hermesLogRepository.queueEvent(
            CopyVaultItemFieldAnonymous(
                field = Field.PASSWORD,
                itemType = ItemType.CREDENTIAL,
                domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
            )
        )
    }

    fun logClickDownload() = hermesLogRepository.queueEvent(DownloadVpnClient())
}