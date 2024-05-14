package com.dashlane.item.subview.provider

import android.content.Context
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.item.subview.readonly.ItemReadSpaceSubView
import com.dashlane.item.subview.readonly.ItemReadValueBooleanSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.item.subview.readonly.ItemReadValueNumberSubView
import com.dashlane.item.subview.readonly.ItemReadValueTextSubView
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem

class ReadOnlySubViewFactory(
    userFeaturesChecker: UserFeaturesChecker,
    currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) : BaseSubViewFactory(userFeaturesChecker, currentTeamSpaceUiFilter) {

    override fun createSubViewString(
        header: String,
        value: String?,
        protected: Boolean,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
        suggestions: List<String>?,
        allowReveal: Boolean,
        multiline: Boolean,
        coloredCharacter: Boolean,
        protectedStateListener: (Boolean) -> Unit
    ): ItemSubView<String>? {
        return value?.takeIf { it.isNotBlank() }
            ?.let {
                ItemReadValueTextSubView(
                    header = header,
                    value = it,
                    protected = protected,
                    allowReveal = allowReveal,
                    multiline = multiline,
                    coloredCharacter = coloredCharacter,
                    protectedStateListener = protectedStateListener
                )
            }
    }

    override fun createSubViewNumber(
        header: String,
        value: String?,
        @SubViewFactory.TYPE inputType: Int,
        protected: Boolean,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
        suggestions: List<String>?,
        protectedStateListener: (Boolean) -> Unit
    ): ItemSubView<String>? {
        return value?.takeIf { it.isNotBlank() }
            ?.let {
                ItemReadValueNumberSubView(
                    header,
                    it,
                    protected = protected,
                    protectedStateListener = protectedStateListener
                )
            }
    }

    override fun createSubviewList(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
    ): ItemSubView<String>? {
        return ItemReadValueListSubView(title, selectedValue, values)
    }

    override fun createSubviewListNonDefault(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
    ): ItemSubView<String>? {
        return ItemReadValueListSubView(title, selectedValue, values)
    }

    override fun createSubviewBoolean(
        header: String,
        description: String?,
        value: Boolean,
        valueUpdate: (VaultItem<*>, Boolean) -> VaultItem<*>?
    ): ItemSubView<Boolean> {
        return ItemReadValueBooleanSubView(header, description, value)
    }

    override fun createSpaceSelector(
        currentSpaceId: String?,
        teamSpaceAccessor: TeamSpaceAccessor,
        toListenViews: List<ItemSubView<String>>?,
        valueUpdate: (VaultItem<*>, TeamSpace) -> VaultItem<*>,
        linkedWebsites: List<String>
    ): ItemSubView<TeamSpace> {
        return ItemReadSpaceSubView(
            getTeamspace(teamSpaceAccessor, currentSpaceId),
            getTeamspaces(teamSpaceAccessor)
        )
    }

    override fun createSubviewDelete(
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        deleteAllowed: Boolean,
        text: String
    ): ItemClickActionSubView? {
        
        return null
    }
}