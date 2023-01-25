package com.dashlane.item.subview.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.AttachmentDetailsAction
import com.dashlane.item.subview.action.ShareDetailsAction
import com.dashlane.item.subview.readonly.ItemExpandableListSubView
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.url.UrlDomain
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.attachmentsAllowed
import com.dashlane.vault.util.attachmentsCount

abstract class BaseSubViewFactory(private val userFeaturesChecker: UserFeaturesChecker) : SubViewFactory {

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
            sharingCount.first!!, sharingCount.first
        )
        val groupCount = context.resources.getQuantityString(
            R.plurals.sharing_shared_counter_groups,
            sharingCount.second!!, sharingCount.second
        )
        val sharingDetailsText = if (sharingCount.first != 0 && sharingCount.second != 0) {
            context.getString(
                R.string.sharing_shared_shared_with_users_and_groups, userCount,
                groupCount
            )
        } else if (sharingCount.first != 0) {
            context.getString(R.string.sharing_shared_shared_with, userCount)
        } else {
            context.getString(R.string.sharing_shared_shared_with, groupCount)
        }

        return ItemSubViewWithActionWrapper(
            ReadOnlySubViewFactory(userFeaturesChecker).createSubViewString(
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
        if (!summary.attachmentsAllowed(userFeaturesChecker)) {
            
            return null
        }
        val attachmentsCount = summary.attachmentsCount()
        if (attachmentsCount == 0) {
            return null
        }
        val attachmentsDetailsText = context.resources.getQuantityString(
            R.plurals.attachment_quantity, attachmentsCount, attachmentsCount
        )
        return ItemSubViewWithActionWrapper(
            ReadOnlySubViewFactory(userFeaturesChecker).createSubViewString(
                context.getString(R.string.attachments_view_section_title),
                attachmentsDetailsText,
                false
            )!!,
            AttachmentDetailsAction(vaultItem)
        )
    }

    override fun createSubviewLinkedDomains(
        context: Context,
        vaultItem: VaultItem<*>,
        expandLinkedDomainsListener: (List<String>) -> Unit
    ): ItemSubView<List<String>>? {
        val summary: SummaryObject = vaultItem.toSummary()
        val authentifiant = summary as? SummaryObject.Authentifiant ?: return null
        val foundLinkedDomains = KnownLinkedDomains.getMatchingLinkedDomainSet(authentifiant.urlDomain)

        return foundLinkedDomains?.let { linkedDomains: Set<UrlDomain> ->
            val title = context.getString(R.string.autofill_related_websites, linkedDomains.size)
            val textList = linkedDomains.map { it.value }

            ItemExpandableListSubView(textList, title, expandLinkedDomainsListener)
        }
    }

    

    fun getTeamspaces(teamspaceAccessor: TeamspaceAccessor): List<Teamspace> {
        return teamspaceAccessor.all.minus(TeamspaceManager.COMBINED_TEAMSPACE)
    }

    

    fun getTeamspace(teamspaceAccessor: TeamspaceAccessor, teamspaceName: String?): Teamspace {
        return teamspaceName?.let {
            
            teamspaceAccessor.all.firstOrNull { it.teamId == teamspaceName }
                ?: TeamspaceManager.PERSONAL_TEAMSPACE 
        } ?: teamspaceAccessor.current 
            ?.takeUnless { it.type == Teamspace.Type.COMBINED } 
        ?: TeamspaceManager.PERSONAL_TEAMSPACE 
    }

    private fun isNotShared(sharingCount: Pair<Int, Int>) = sharingCount.first == 0 && sharingCount.second == 0
}