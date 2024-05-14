package com.dashlane.collections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.TextFieldActions.ClearField
import com.dashlane.design.theme.DashlaneTheme

@Composable
internal fun SearchableTopAppBarTitle(
    showSearch: Boolean,
    searchLabel: String,
    title: String,
    searchQuery: MutableState<String>,
    isSearching: MutableState<Boolean>
) {
    if (showSearch) {
        isSearching.value = true
        TextField(
            value = searchQuery.value,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { if (isSearching.value) searchQuery.value = it },
            label = searchLabel,
            labelPersists = false,
            actions = ClearField(
                contentDescription = stringResource(id = R.string.and_accessibility_action_text_clear),
                onClick = {
                    searchQuery.value = ""
                    true
                }
            )
        )
    } else {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = DashlaneTheme.typography.titleSectionMedium
        )
    }
}