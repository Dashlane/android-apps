package com.dashlane.item.subview.view

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.widgets.view.CategoryChip
import com.dashlane.ui.widgets.view.CategoryChipList

object CollectionListViewProvider {

    fun create(context: Context, item: ItemCollectionListSubView): View {
        (item.teamspaceView as? ItemEditSpaceSubView)?.addValueChangedListener(object :
            ValueChangeManager.Listener<TeamSpace> {
            override fun onValueChanged(origin: Any, newValue: TeamSpace) {
                item.value.value = emptyList()
                
                
                
                
                
                
                item.header.value = when (item.header.value) {
                    context.getString(R.string.collections_header_business_item_edit) ->
                        context.getString(R.string.collections_header_personal_item_edit)
                    context.getString(R.string.collections_header_personal_item_edit) ->
                        context.getString(R.string.collections_header_business_item_edit)
                    else -> context.getString(R.string.collections_header_item_edit)
                }
            }
        })
        return ComposeView(context).apply {
            
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ItemEditCollectionList(item)
            }
        }
    }

    @Composable
    fun ItemEditCollectionList(item: ItemCollectionListSubView) {
        val collections = item.value
        DashlaneTheme {
            Column(modifier = Modifier.padding(start = 3.75.dp)) {
                Text(
                    text = item.header.value,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    style = DashlaneTheme.typography.bodyHelperRegular
                )
                Spacer(modifier = Modifier.size(8.dp))

                if (collections.value.isNotEmpty()) {
                    CategoryChipList {
                        collections.value.forEach { collection ->
                            val canDelete = !collection.shared || item.canUpdateSharedCollection
                            val editMode = item.editMode && canDelete
                            CategoryChip(
                                label = collection.name,
                                editMode = editMode,
                                shared = collection.shared,
                                onClick = {
                                    if (editMode) {
                                        item.value.value = collections.value.filterNot {
                                            (collection.id != null && it.id == collection.id) ||
                                                (collection.id == null && it.name == collection.name)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                if (item.editMode || collections.value.isEmpty()) {
                    ButtonAddCollection(item, collections.value)
                }
            }
        }
    }

    @Composable
    private fun ButtonAddCollection(
        item: ItemCollectionListSubView,
        collections: List<Collection>
    ) {
        ButtonMedium(
            onClick = {
                item.listener?.openCollectionSelector(
                    fromViewOnly = !item.editMode,
                    temporaryPrivateCollectionsName = collections.filter { !it.shared }
                        .map { it.name },
                    temporarySharedCollectionsId = collections.filter { it.shared }
                        .mapNotNull { it.id },
                    spaceId = item.teamspaceView?.value?.teamId ?: TeamSpace.Personal.teamId ?: ""
                )
            },
            layout = if (item.editMode) {
                ButtonLayout.IconLeading(
                    iconToken = IconTokens.actionAddOutlined,
                    stringResource(id = R.string.collection_list_view_provider_add_button)
                )
            } else {
                ButtonLayout.TextOnly(stringResource(id = R.string.collection_list_view_provider_add_button))
            },
            intensity = Intensity.Supershy
        )
    }
}