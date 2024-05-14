package com.dashlane.teamspaces.util

import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.CurrentTeam
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.PastTeam
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.PastTeam.Status
import com.dashlane.server.api.time.InstantEpochSecond
import com.dashlane.teamspaces.model.TeamSpace

object BusinessSpaceUtil {
    fun createCurrentSpace(
        name: String,
        teamId: Long,
        color: String
    ): TeamSpace.Business.Current {
        val space = CurrentTeam(
            teamName = name,
            teamMembership = CurrentTeam.TeamMembership(
                isSSOUser = true,
                isBillingAdmin = false,
                isGroupManager = false,
                teamAdmins = listOf(),
                isTeamAdmin = false,
                billingAdmins = listOf()
            ),
            planName = "",
            teamInfo = CurrentTeam.TeamInfo(
                color = color,
                membersNumber = 1,
                planType = "",
            ),
            joinDateUnix = InstantEpochSecond(0),
            teamId = teamId,
            planFeature = CurrentTeam.PlanFeature.TEAM,
            isSoftDiscontinued = false,
        )
        return TeamSpace.Business.Current(space)
    }

    fun createPastTeam(
        name: String = "team",
        teamId: Long = 1234,
        domains: List<String>? = null,
        forceDomain: Boolean? = null,
        removeForcedContent: Boolean? = null
    ): TeamSpace.Business.Past {
        val space = PastTeam(
            teamName = name,
            teamMembership = PastTeam.TeamMembership(
                isSSOUser = true,
                isBillingAdmin = false,
                isGroupManager = false,
                teamAdmins = listOf(),
                isTeamAdmin = false,
                billingAdmins = listOf()
            ),
            teamInfo = PastTeam.TeamInfo(
                membersNumber = 1,
                planType = "",
                teamDomains = domains,
                forcedDomainsEnabled = forceDomain,
                removeForcedContentEnabled = removeForcedContent
            ),
            joinDateUnix = InstantEpochSecond(0),
            teamId = teamId,
            planFeature = PastTeam.PlanFeature.TEAM,
            revokeDateUnix = InstantEpochSecond(0),
            status = Status.REVOKED
        )
        return TeamSpace.Business.Past(space)
    }
}