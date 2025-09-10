package com.dashlane.teamspaces.manager

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.getB2bStatus
import com.dashlane.accountstatus.hasEnforcedTeamSpace
import com.dashlane.accountstatus.isB2bUser
import com.dashlane.accountstatus.isVaultExportEnabled
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.CurrentTeam.TeamInfo.TwoFAEnforced
import com.dashlane.session.Session
import com.dashlane.teamspaces.export.ExportVaultStatus
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
    val accountStatusRepository: AccountStatusRepository,
) : TeamSpaceAccessor {
    private val accountStatus: AccountStatus
        get() = accountStatusRepository[session] ?: AccountStatusRepository.DEFAULT_ACCOUNT_STATUS

    private val b2bStatus: PremiumStatus.B2bStatus?
        get() = accountStatus.getB2bStatus()

    override val pastBusinessTeams: List<TeamSpace.Business.Past>
        get() = b2bStatus?.pastTeams?.map { TeamSpace.Business.Past(it) } ?: listOf()

    override val currentBusinessTeam: TeamSpace.Business.Current?
        get() = b2bStatus?.getCurrentTeam()

    override val hasEnforcedTeamSpace: Boolean = accountStatus.hasEnforcedTeamSpace()

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
        get() = accountStatus.isForcedDomainsEnabled()
    override val isVaultExportEnabled: Boolean
        get() = accountStatus.isVaultExportEnabled()

    override val canChangeTeamspace: Boolean = currentBusinessTeam != null && !hasEnforcedTeamSpace

    override val isSsoUser: Boolean
        get() = currentBusinessTeam?.space?.teamMembership?.isSSOUser == true

    override val is2FAEnforced: Boolean
        get() = accountStatus.is2FAEnforced()

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

fun AccountStatus.isForcedDomainsEnabled(): Boolean = getCurrentTeam()?.isForcedDomainsEnabled == true

fun PremiumStatus.B2bStatus.getCurrentTeam(): TeamSpace.Business.Current? = this.currentTeam?.let { TeamSpace.Business.Current(it) }

fun AccountStatus.getCurrentTeam(): TeamSpace.Business.Current? = getB2bStatus()?.getCurrentTeam()

fun AccountStatus.getCryptoForcedPayload(): String? = getB2bStatus()?.getCurrentTeam()?.cryptoForcedPayload

fun AccountStatus.is2FAEnforced(): Boolean = when (getCurrentTeam()?.space?.teamInfo?.twoFAEnforced) {
    TwoFAEnforced.LOGIN,
    TwoFAEnforced.NEWDEVICE,
    -> true
    else -> false
}

fun AccountStatus.exportVaultStatus(): ExportVaultStatus {
    if (!isB2bUser()) return ExportVaultStatus.COMPLETE
    return when {
        hasEnforcedTeamSpace() -> {
            if (isVaultExportEnabled()) ExportVaultStatus.COMPLETE else ExportVaultStatus.DISABLED
        }
        isForcedDomainsEnabled() -> ExportVaultStatus.LIMITED
        else -> ExportVaultStatus.COMPLETE
    }
}