package com.dashlane.collections.sharing

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.collections.SearchableTopAppBarTitle
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionSharingTopAppBar(
    title: String,
    searchHint: String,
    uiState: CollectionSharingViewState,
    viewData: CollectionSharingViewState.ViewData,
    searchQuery: MutableState<String>,
    isSearching: MutableState<Boolean>,
    listener: CollectionSharingAppBarListener,
) {
    TopAppBar(
        title = {
            SearchableTopAppBarTitle(
                showSearch = uiState is CollectionSharingViewState.ShowList && viewData.showSearch,
                searchLabel = searchHint,
                title = title,
                searchQuery = searchQuery,
                isSearching = isSearching,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (viewData.showSearch) {
                        
                        isSearching.value = false
                        searchQuery.value = ""
                        listener.onToggleSearch()
                    } else {
                        listener.onCloseClicked()
                    }
                },
            ) {
                Icon(
                    token = if (viewData.showSearch) {
                        IconTokens.arrowLeftOutlined
                    } else {
                        IconTokens.actionCloseOutlined
                    },
                    contentDescription = stringResource(id = R.string.and_accessibility_close),
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        actions = {
            ButtonMedium(
                onClick = {
                    
                    listener.onToggleSearch()
                },
                layout = ButtonLayout.IconOnly(
                    IconTokens.actionSearchOutlined,
                    stringResource(R.string.collection_new_share_search_accessibility),
                ),
                mood = Mood.Neutral,
                intensity = Intensity.Supershy,
            )
        },
        colors = DashlaneTopAppBar.defaultColors,
    )
}

internal interface CollectionSharingAppBarListener {
    fun onCloseClicked()

    fun onToggleSearch()
}