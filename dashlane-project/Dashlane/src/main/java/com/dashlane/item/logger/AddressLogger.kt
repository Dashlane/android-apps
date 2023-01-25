package com.dashlane.item.logger

import com.dashlane.device.DeviceInfoRepository
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode12
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.Constants
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.vault.util.getCountry
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.util.Locale

class AddressLogger(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val deviceInfoRepository: DeviceInfoRepository,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    @Suppress("UNCHECKED_CAST")
    override fun logItemAdded(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemAdded(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.Address) return
        sendUsageLog12(vaultItem as VaultItem<SyncObject.Address>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemModified(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.Address) return
        sendUsageLog12(vaultItem as VaultItem<SyncObject.Address>)
    }

    private fun sendUsageLog12(vaultItem: VaultItem<SyncObject.Address>) {
        val teamspace = teamspaceAccessor.get(TeamSpaceUtils.getTeamSpaceId(vaultItem))
        val item = vaultItem.syncObject
        log(
            UsageLogCode12(
                spaceId = teamspace?.anonTeamId,
                oslang = Constants.getOSLang(),
                lang = Constants.getLang(),
                osformat = deviceInfoRepository.deviceCountry,
                zipcode = item.zipCode,
                country = item.getCountry().isoCode,
                format = Locale.getDefault().country
            )
        )
    }
}