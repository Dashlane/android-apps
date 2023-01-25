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
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.util.Locale

class IdentityLogger(
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
        if (vaultItem.syncObject !is SyncObject.Identity) return
        sendUsageLog12(vaultItem as VaultItem<SyncObject.Identity>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemModified(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.Identity) return
        sendUsageLog12(vaultItem as VaultItem<SyncObject.Identity>)
    }

    private fun sendUsageLog12(item: VaultItem<SyncObject.Identity>) {
        log(
            UsageLogCode12(
                spaceId = teamspaceAccessor.get(TeamSpaceUtils.getTeamSpaceId(item))?.anonTeamId,
                oslang = Constants.getOSLang(),
                lang = Constants.getLang(),
                osformat = deviceInfoRepository.deviceCountry,
                format = Locale.getDefault().country,
                gender = when (item.syncObject.title) {
                    SyncObject.Identity.Title.MME, SyncObject.Identity.Title.MS, SyncObject.Identity.Title.MLLE -> UsageLogCode12.Gender.FEMALE
                    SyncObject.Identity.Title.MR -> UsageLogCode12.Gender.MALE
                    else -> null
                },
                birth = item.syncObject.birthDate?.year
            )
        )
    }
}