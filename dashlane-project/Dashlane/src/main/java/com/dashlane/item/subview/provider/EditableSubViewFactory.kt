package com.dashlane.item.subview.provider

import android.content.Context
import android.view.Gravity
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.edit.ItemEditValueBooleanSubView
import com.dashlane.item.subview.edit.ItemEditValueListNonDefaultSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem

class EditableSubViewFactory(userFeaturesChecker: UserFeaturesChecker) : BaseSubViewFactory(userFeaturesChecker) {

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
    ): ItemSubView<String> {
        return ItemEditValueTextSubView(
            header,
            value ?: "",
            protected,
            valueUpdate,
            suggestions,
            allowReveal,
            multiline,
            coloredCharacter,
            protectedStateListener = protectedStateListener
        )
    }

    override fun createSubViewNumber(
        header: String,
        value: String?,
        @SubViewFactory.TYPE inputType: Int,
        protected: Boolean,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
        suggestions: List<String>?,
        protectedStateListener: (Boolean) -> Unit
    ): ItemSubView<String> {
        return ItemEditValueNumberSubView(
            header,
            value ?: "",
            protected,
            inputType,
            valueUpdate,
            suggestions,
            protectedStateListener
        )
    }

    override fun createSubviewList(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
    ): ItemSubView<String> {
        return ItemEditValueListSubView(title, selectedValue, values, valueUpdate)
    }

    override fun createSubviewListNonDefault(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
    ): ItemSubView<String>? {
        return ItemEditValueListNonDefaultSubView(title, selectedValue, values, valueUpdate)
    }

    override fun createSubviewBoolean(
        header: String,
        description: String?,
        value: Boolean,
        valueUpdate: (VaultItem<*>, Boolean) -> VaultItem<*>?
    ): ItemSubView<Boolean> {
        return ItemEditValueBooleanSubView(header, description, value, valueUpdate)
    }

    override fun createSpaceSelector(
        current: String?,
        teamspaceAccessor: TeamspaceAccessor,
        toListenViews: List<ItemSubView<String>>?,
        valueUpdate: (VaultItem<*>, Teamspace) -> VaultItem<*>,
        linkedWebsites: List<String>,
    ): ItemSubView<Teamspace> {
        return ItemEditSpaceSubView(
            getTeamspace(teamspaceAccessor, current),
            getTeamspaces(teamspaceAccessor),
            current == null,
            toListenViews,
            linkedWebsites,
            valueUpdate
        )
    }

    override fun createSubviewDelete(
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        deleteAllowed: Boolean,
        text: String
    ): ItemClickActionSubView? {
        if (!deleteAllowed) return null
        return ItemClickActionSubView(text, Mood.Danger, Intensity.Catchy, gravity = Gravity.END) {
            listener.notifyDeleteClicked()
        }
    }
}