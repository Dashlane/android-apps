package com.dashlane.teamspaces.model

import androidx.core.graphics.toColorInt
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.teamspaces.R
import com.dashlane.teamspaces.model.SpaceName.TeamName

sealed class TeamSpace {

    abstract val teamId: String?

    abstract val name: SpaceName

    abstract val color: SpaceColor

    abstract val displayLetter: Char

    abstract val domains: List<String>

    data object Combined : TeamSpace() {
        override val teamId: String?
            get() = null

        override val name: SpaceName
            get() = SpaceName.FixName(R.string.teamspace_combined_title)

        override val color: SpaceColor
            get() = SpaceColor.FixColor(R.color.teamspace_combined)

        override val displayLetter: Char
            get() = ' '

        override val domains: List<String> = listOf()
    }

    data object Personal : TeamSpace() {

        override val teamId: String?
            get() = null

        override val name: SpaceName
            get() = SpaceName.FixName(R.string.teamspace_personal_title)

        override val color: SpaceColor
            get() = SpaceColor.FixColor(R.color.teamspace_personal)

        override val displayLetter: Char
            get() = 'P'

        override val domains: List<String> = listOf()
    }

    sealed class Business : TeamSpace() {

        abstract override val teamId: String

        val cryptoForcedPayload: String?
            get() = when (this) {
                is Current -> space.teamInfo.cryptoForcedPayload
                is Past -> space.teamInfo.cryptoForcedPayload
            }

        val isLockOnExitEnabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.lockOnExit == true
                is Past -> space.teamInfo.lockOnExit == true
            }

        val isSharingDisabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.sharingDisabled == true
                is Past -> space.teamInfo.sharingDisabled == true
            }

        val isEmergencyDisabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.emergencyDisabled == true
                is Past -> space.teamInfo.emergencyDisabled == true
            }

        val isForcedDomainsEnabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.forcedDomainsEnabled == true
                is Past -> space.teamInfo.forcedDomainsEnabled == true
            }

        val isRichIconsEnabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.richIconsEnabled == true
                is Past -> space.teamInfo.richIconsEnabled == true
            }

        val isCollectSensitiveDataActivityLogsEnabled: Boolean
            get() = when (this) {
                is Current -> space.teamInfo.collectSensitiveDataAuditLogsEnabled == true
                is Past -> space.teamInfo.collectSensitiveDataAuditLogsEnabled == true
            }

        data class Current(val space: PremiumStatus.B2bStatus.CurrentTeam) : Business() {
            override val teamId: String
                get() = space.teamId.toString()

            override val name: TeamName
                get() = TeamName(space.teamName ?: "")

            override val color: SpaceColor
                get() = SpaceColor.TeamColor(space.teamInfo.color?.toColorInt() ?: -1)

            override val displayLetter: Char
                get() = space.teamInfo.letter?.toCharArray()?.getOrNull(0)
                    ?: name.value.toCharArray().getOrNull(0)
                    ?: ' '

            override val domains: List<String> = space.teamInfo.teamDomains ?: listOf()

            val hasPersonalSpace: Boolean = space.teamInfo?.personalSpaceEnabled == true
        }

        data class Past(val space: PremiumStatus.B2bStatus.PastTeam) : Business() {
            override val teamId: String
                get() = space.teamId.toString()

            override val name: TeamName
                get() = TeamName(space.teamName ?: "")

            override val color: SpaceColor
                get() = SpaceColor.TeamColor(space.teamInfo.color?.toColorInt() ?: -1)

            override val displayLetter: Char
                get() = space.teamInfo.letter?.toCharArray()?.getOrNull(0)
                    ?: name.value.toCharArray().getOrNull(0)
                    ?: ' '

            val domainsToExcludeNow: List<String>
                get() = if (isRemovedBusinessContentEnabled) {
                    space.teamInfo.teamDomains ?: emptyList()
                } else {
                    emptyList()
                }

            override val domains: List<String> = space.teamInfo.teamDomains ?: listOf()

            val isRemovedBusinessContentEnabled: Boolean
                get() = space.teamInfo.removeForcedContentEnabled == true

            val shouldDelete: Boolean = space.shouldDelete == true
        }
    }
}