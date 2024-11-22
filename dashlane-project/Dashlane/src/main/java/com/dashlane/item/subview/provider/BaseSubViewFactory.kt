package com.dashlane.item.subview.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.featureflipping.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.AttachmentDetailsAction
import com.dashlane.item.subview.action.ShareDetailsAction
import com.dashlane.securefile.extensions.attachmentsCount
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.utils.attachmentsAllowed

abstract class BaseSubViewFactory(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val currentTeamSpaceFilter: CurrentTeamSpaceUiFilter
) : SubViewFactory {

    override fun createSubviewSharingDetails(
        context: Context,
        vaultItem: VaultItem<*>,
        sharingPolicy: SharingPolicyDataProvider
    ): ItemSubView<String>? {
        val uid = vaultItem.uid
        val sharingCount = sharingPolicy.getSharingCount(uid)
        if (isNotShared(sharingCount)) {
            return null
        }
        val userCount = context.resources.getQuantityString(
            R.plurals.sharing_shared_counter_users,
            sharingCount.first,
            sharingCount.first
        )
        val groupCount = context.resources.getQuantityString(
            R.plurals.sharing_shared_counter_groups,
            sharingCount.second,
            sharingCount.second
        )
        val sharingDetailsText = if (sharingCount.first != 0 && sharingCount.second != 0) {
            context.getString(
                R.string.sharing_shared_shared_with_users_and_groups,
                userCount,
                groupCount
            )
        } else if (sharingCount.first != 0) {
            context.getString(R.string.sharing_shared_shared_with, userCount)
        } else {
            context.getString(R.string.sharing_shared_shared_with, groupCount)
        }

        return ItemSubViewWithActionWrapper(
            ReadOnlySubViewFactory(userFeaturesChecker, currentTeamSpaceFilter).createSubViewString(
                context.getString(R.string.sharing_services_view_section_title),
                sharingDetailsText,
                false
            )!!,
            ShareDetailsAction(vaultItem)
        )
    }

    override fun createSubviewAttachmentDetails(
        context: Context,
        vaultItem: VaultItem<*>
    ): ItemSubView<String>? {
        val summary: SummaryObject = vaultItem.toSummary()
        if (!summary.attachmentsAllowed(attachmentAllItems = userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS))) {
            
            return null
        }
        val attachmentsCount = summary.attachmentsCount()
        if (attachmentsCount == 0) {
            return null
        }
        val attachmentsDetailsText = context.resources.getQuantityString(
            R.plurals.attachment_quantity,
            attachmentsCount,
            attachmentsCount
        )
        return ItemSubViewWithActionWrapper(
            ReadOnlySubViewFactory(userFeaturesChecker, currentTeamSpaceFilter).createSubViewString(
                context.getString(R.string.attachments_view_section_title),
                attachmentsDetailsText,
                false
            )!!,
            AttachmentDetailsAction(vaultItem)
        )
    }

    fun getTeamspaces(teamSpaceAccessor: TeamSpaceAccessor): List<TeamSpace> {
        return teamSpaceAccessor.availableSpaces.minus(TeamSpace.Combined)
    }

    fun getTeamspace(teamSpaceAccessor: TeamSpaceAccessor, spaceId: String?): TeamSpace {
        return spaceId?.let {
            
            teamSpaceAccessor.availableSpaces.firstOrNull { it.teamId == spaceId }
                ?: TeamSpace.Personal 
        } 
            ?: currentTeamSpaceFilter.currentFilter.teamSpace.takeUnless { it == TeamSpace.Combined }
            
            ?: TeamSpace.Personal 
    }

    private fun isNotShared(sharingCount: Pair<Int, Int>) = sharingCount.first == 0 && sharingCount.second == 0
}