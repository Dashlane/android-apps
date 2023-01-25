package com.dashlane.auditduplicates

import com.dashlane.auditduplicates.grouping.AuthentifiantForGrouping
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.xml.domain.SyncObject



interface ReloadAuthentifiantWithMoreDetail {
    fun reload(currentAuthentifiant: AuthentifiantForGrouping): AuthentifiantForGrouping
}



class ReloadSameAuthentifiant : ReloadAuthentifiantWithMoreDetail {
    override fun reload(currentAuthentifiant: AuthentifiantForGrouping): AuthentifiantForGrouping = currentAuthentifiant
}



class ReloadAuthentifiantWithMoreDetailFromVault(
    val vaultDataQuery: VaultDataQuery
) : ReloadAuthentifiantWithMoreDetail {

    override fun reload(currentAuthentifiant: AuthentifiantForGrouping): AuthentifiantForGrouping {
        try {
            val fullAuthentifiant = vaultDataQuery.query(
                vaultFilter { specificUid(currentAuthentifiant.authentifiant.id) }
            )?.syncObject as? SyncObject.Authentifiant ?: throw AuditDuplicatesException("could not load more detail")

            return AuthentifiantForGrouping(fullAuthentifiant)
        } catch (e: Exception) {
            throw AuditDuplicatesException("error on loading more detail", e)
        }
    }
}
