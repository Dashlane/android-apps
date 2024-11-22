package com.dashlane.collections

import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

internal fun SummaryObject.spaceData(teamSpaceAccessorProvider: TeamSpaceAccessorProvider): SpaceData? =
    spaceData(spaceId, teamSpaceAccessorProvider)

internal fun businessSpaceData(teamSpaceAccessorProvider: TeamSpaceAccessorProvider) =
    teamSpaceAccessorProvider.get()?.availableSpaces
        ?.minus(setOf(TeamSpace.Combined, TeamSpace.Personal))?.first()?.spaceData()

internal fun SyncObject.Collection.spaceData(teamSpaceAccessorProvider: TeamSpaceAccessorProvider) =
    spaceData(spaceId, teamSpaceAccessorProvider)

private fun TeamSpace.spaceData() = SpaceData(
    spaceLetter = displayLetter,
    spaceColor = color,
    spaceContentDescriptionResId = if (teamId == null || teamId == TeamSpace.Personal.teamId) {
        R.string.and_accessibility_collection_list_item_personal_teamspace
    } else {
        R.string.and_accessibility_collection_list_item_business_teamspace
    },
    businessSpace = teamId != null && teamId != TeamSpace.Personal.teamId
)

private fun spaceData(spaceId: String?, teamSpaceAccessorProvider: TeamSpaceAccessorProvider): SpaceData? {
    val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return null

    val teamspace = if (teamSpaceAccessor.canChangeTeamspace) {
        teamSpaceAccessor.availableSpaces.minus(TeamSpace.Combined)
            .find { it.teamId == spaceId } ?: TeamSpace.Personal
    } else {
        null
    }
    return teamspace?.spaceData()
}

data class SpaceData(
    val spaceLetter: Char,
    val spaceColor: SpaceColor,
    @StringRes val spaceContentDescriptionResId: Int,
    val businessSpace: Boolean
)
