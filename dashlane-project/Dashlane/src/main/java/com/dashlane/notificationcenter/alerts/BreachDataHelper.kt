package com.dashlane.notificationcenter.alerts

import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class BreachDataHelper @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val appEvents: AppEvents
) {

    @Suppress("UNCHECKED_CAST")
    suspend fun saveAndRemove(breachWrapper: BreachWrapper, breachStatus: SyncObject.SecurityBreach.Status) {
        val securityBreach = breachWrapper.localBreach
        val filter = vaultFilter {
            specificUid(securityBreach.uid)
            specificDataType(SyncObjectType.SECURITY_BREACH)
        }
        val breach = vaultDataQuery.query(filter) as? VaultItem<SyncObject.SecurityBreach> ?: return
        dataSaver.save(
            breach
                .copySyncObject { status = breachStatus }
                .copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }
        )
        appEvents.post(BreachStatusChangedEvent())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun markAllAsViewed(list: List<BreachWrapper>) {
        list.filter { it.localBreach.status == SyncObject.SecurityBreach.Status.PENDING }
            .forEach {
                val filter = vaultFilter {
                    specificUid(it.localBreach.uid)
                    specificDataType(SyncObjectType.SECURITY_BREACH)
                }
                val breach = vaultDataQuery.query(filter) as? VaultItem<SyncObject.SecurityBreach> ?: return
                dataSaver.save(
                    breach.copySyncObject {
                        status = SyncObject.SecurityBreach.Status.VIEWED
                    }
                )
            }
    }
}
