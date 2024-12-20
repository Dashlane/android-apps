package com.dashlane.item.subview.provider

import android.content.Context
import androidx.annotation.IntDef
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem

interface SubViewFactory {
    fun createSubViewString(
        header: String,
        value: String?,
        protected: Boolean = false,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>? = { _, _ -> null },
        suggestions: List<String>? = null,
        allowReveal: Boolean = true,
        multiline: Boolean = false,
        coloredCharacter: Boolean = false,
        protectedStateListener: (Boolean) -> Unit = {}
    ): ItemSubView<String>?

    fun createSubViewNumber(
        header: String,
        value: String?,
        @TYPE inputType: Int,
        protected: Boolean,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>? = { _, _ -> null },
        suggestions: List<String>? = null,
        protectedStateListener: (Boolean) -> Unit = {}
    ): ItemSubView<String>?

    fun createSubviewList(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>? = { _, _ -> null }
    ): ItemSubView<String>?

    fun createSubviewListNonDefault(
        title: String,
        selectedValue: String,
        values: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>? = { _, _ -> null }
    ): ItemSubView<String>?

    fun createSubviewBoolean(
        header: String,
        description: String?,
        value: Boolean,
        valueUpdate: (VaultItem<*>, Boolean) -> VaultItem<*>? = { _, _ -> null }
    ): ItemSubView<Boolean>?

    fun createSpaceSelector(
        currentSpaceId: String?,
        teamSpaceAccessor: TeamSpaceAccessor,
        toListenViews: List<ItemSubView<String>>?,
        valueUpdate: (VaultItem<*>, TeamSpace) -> VaultItem<*>,
        linkedWebsites: List<String> = listOf()
    ): ItemSubView<TeamSpace>?

    fun createSubviewDelete(
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        deleteAllowed: Boolean,
        text: String = context.getString(R.string.delete)
    ): ItemClickActionSubView?

    fun createSubviewSharingDetails(
        context: Context,
        vaultItem: VaultItem<*>,
        sharingPolicy: SharingPolicyDataProvider
    ): ItemSubView<String>?

    fun createSubviewAttachmentDetails(context: Context, vaultItem: VaultItem<*>): ItemSubView<String>?

    companion object {
        const val INPUT_TYPE_NUMBER = 1
        const val INPUT_TYPE_PHONE = 2
    }

    @IntDef(INPUT_TYPE_NUMBER, INPUT_TYPE_PHONE)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TYPE
}