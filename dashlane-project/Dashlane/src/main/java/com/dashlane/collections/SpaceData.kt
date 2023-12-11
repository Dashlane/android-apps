package com.dashlane.collections

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.teamspaces.CombinedTeamspace
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

internal fun SummaryObject.spaceData(teamspaceAccessorProvider: TeamspaceAccessorProvider): SpaceData? {
    val teamspace = teamspaceAccessorProvider.get()?.let { teamspaceAccessor ->
        if (teamspaceAccessor.canChangeTeamspace()) {
            teamspaceAccessor.getOrDefault(spaceId)
        } else {
            null
        }
    }
    return teamspace?.spaceData()
}

internal fun businessSpaceData(teamspaceAccessorProvider: TeamspaceAccessorProvider) =
    teamspaceAccessorProvider.get()?.all
        ?.minus(setOf(CombinedTeamspace, PersonalTeamspace))?.first()?.spaceData()

internal fun SyncObject.Collection.spaceData(teamspaceAccessorProvider: TeamspaceAccessorProvider) =
    toSummary<SummaryObject.Collection>().spaceData(teamspaceAccessorProvider)

private fun Teamspace.spaceData() = SpaceData(
    spaceLetter = displayLetter.firstOrNull() ?: ' ',
    spaceColor = colorInt,
    spaceContentDescriptionResId = if (teamId == PersonalTeamspace.teamId) {
        R.string.and_accessibility_collection_list_item_personal_teamspace
    } else {
        R.string.and_accessibility_collection_list_item_business_teamspace
    },
    businessSpace = teamId != PersonalTeamspace.teamId
)

data class SpaceData(
    val spaceLetter: Char,
    @ColorInt val spaceColor: Int,
    @StringRes val spaceContentDescriptionResId: Int,
    val businessSpace: Boolean
)
