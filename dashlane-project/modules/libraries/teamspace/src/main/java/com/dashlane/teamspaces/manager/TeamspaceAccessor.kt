package com.dashlane.teamspaces.manager

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.CurrentTeam.TeamInfo.TwoFAEnforced
import com.dashlane.session.Session
import com.dashlane.teamspaces.model.TeamSpace

interface TeamSpaceAccessor {
    val isB2bUser: Boolean

    val availableSpaces: List<TeamSpace>

    val currentBusinessTeam: TeamSpace.Business.Current?

    val pastBusinessTeams: List<TeamSpace.Business.Past>

    val allBusinessSpaces: List<TeamSpace.Business>

    val hasEnforcedTeamSpace: Boolean

    val enforcedSpace: TeamSpace.Business.Current?

    val canChangeTeamspace: Boolean

    val isSsoUser: Boolean

    val is2FAEnforced: Boolean

    val cryptoForcedPayload: String?

    val isSharingDisabled: Boolean

    val isLockOnExitEnabled: Boolean

    val isForcedDomainsEnabled: Boolean

    val isVaultExportEnabled: Boolean

    val isRichIconsEnabled: Boolean

    fun getOrDefault(teamId: String?): TeamSpace

    fun get(teamId: String?): TeamSpace.Business?
}

class TeamSpaceAccessorImpl(
    private val session: Session,
    val accountStatusRepository: AccountStatusRepository
) : TeamSpaceAccessor {

    private val accountStatus: AccountStatus
        get() = accountStatusRepository[session] ?: AccountStatusRepository.DEFAULT_ACCOUNT_STATUS

    private val b2bStatus: PremiumStatus.B2bStatus?
        get() = accountStatus.premiumStatus.b2bStatus

    override val pastBusinessTeams: List<TeamSpace.Business.Past>
        get() = b2bStatus?.pastTeams?.map { TeamSpace.Business.Past(it) } ?: listOf()

    override val currentBusinessTeam: TeamSpace.Business.Current?
        get() = b2bStatus?.currentTeam?.let { TeamSpace.Business.Current(it) }

    override val hasEnforcedTeamSpace: Boolean = b2bStatus?.currentTeam?.teamInfo?.personalSpaceEnabled == false

    override val enforcedSpace: TeamSpace.Business.Current?
        get() = if (hasEnforcedTeamSpace) {
            b2bStatus?.currentTeam?.let { TeamSpace.Business.Current(it) }
        } else {
            null
        }

    override val cryptoForcedPayload: String?
        get() = currentBusinessTeam?.cryptoForcedPayload

    override val isSharingDisabled: Boolean
        get() = currentBusinessTeam?.isSharingDisabled == true

    override val isRichIconsEnabled: Boolean
        get() = currentBusinessTeam?.isRichIconsEnabled != false

    override val isLockOnExitEnabled: Boolean
        get() = currentBusinessTeam?.isLockOnExitEnabled == true
    override val isForcedDomainsEnabled: Boolean
        get() = currentBusinessTeam?.isForcedDomainsEnabled == true
    override val isVaultExportEnabled: Boolean
        get() = b2bStatus?.currentTeam?.teamInfo?.vaultExportEnabled != false

    override val canChangeTeamspace: Boolean = currentBusinessTeam != null && !hasEnforcedTeamSpace

    override val isSsoUser: Boolean
        get() = currentBusinessTeam?.space?.teamMembership?.isSSOUser == true

    override val is2FAEnforced: Boolean
        get() = when (currentBusinessTeam?.space?.teamInfo?.twoFAEnforced) {
            TwoFAEnforced.LOGIN,
            TwoFAEnforced.NEWDEVICE -> true
            else -> false
        }

    override val isB2bUser: Boolean
        get() = b2bStatus?.currentTeam != null

    override val availableSpaces: List<TeamSpace>
        get() {
            val currentTeam = currentBusinessTeam
            return when {
                hasEnforcedTeamSpace && currentTeam != null -> listOf<TeamSpace>(currentTeam)
                currentTeam != null -> listOf(TeamSpace.Combined, TeamSpace.Personal, currentTeam)
                else -> listOf(TeamSpace.Personal)
            }
        }

    override val allBusinessSpaces: List<TeamSpace.Business>
        get() = pastBusinessTeams.plus(currentBusinessTeam).filterNotNull()

    override fun getOrDefault(teamId: String?): TeamSpace {
        return get(teamId) ?: TeamSpace.Combined
    }

    override fun get(teamId: String?): TeamSpace.Business? {
        return allBusinessSpaces.firstOrNull {
            it.teamId == teamId
        }
    }
}
