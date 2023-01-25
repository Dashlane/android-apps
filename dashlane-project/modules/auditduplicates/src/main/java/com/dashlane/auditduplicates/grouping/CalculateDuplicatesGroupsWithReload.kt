package com.dashlane.auditduplicates.grouping

import com.dashlane.auditduplicates.ReloadAuthentifiantWithMoreDetail
import com.dashlane.util.valueWithoutWww



class CalculateDuplicatesGroupsWithReload(
    private val reloadAuthentifiantWithMoreDetail: ReloadAuthentifiantWithMoreDetail
) : CalculateDuplicatesGroups {

    

    override fun calculateGroups(accounts: List<AuthentifiantForGrouping>): Pair<GroupHeader, List<GroupDetail>> {
        val serviceByIdentityDuplicates = accounts.duplicatesGroupedByServiceByIdentity()
        val groupDetailList = serviceByIdentityDuplicates.map { serviceGroup ->
            serviceGroup.map { identityGroup ->
                identityGroup.map {
                    reloadAuthentifiantWithMoreDetail.reload(it)
                }.calculateGroupDetail()
            }
        }.flatten()

        val groupHeader = GroupHeader(
            accounts.size,
            groupDetailList.sumOf { it.groupNbCredentials },
            groupDetailList.size
        )

        return groupHeader to groupDetailList
    }

    private fun List<AuthentifiantForGrouping>.calculateGroupDetail(): GroupDetail {
        val nbCredentials = this.size
        val nbDiffHosts = this.groupBy { it.urlDomain?.valueWithoutWww() }.size
        val nbDiffPasswords = this.groupBy { it.password }.size
        
        val nbExactDuplicates = this.groupBy { authentifiant ->
            authentifiant.exactDuplicatesData
        }.filter {
            it.value.size > 1
        }.map {
            it.value.size
        }.sum()

        return GroupDetail(nbCredentials, nbExactDuplicates, nbDiffHosts, nbDiffPasswords)
    }
}
