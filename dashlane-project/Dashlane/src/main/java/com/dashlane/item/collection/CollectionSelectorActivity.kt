package com.dashlane.item.collection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.navArgs
import com.dashlane.R
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BadgeIcon
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.collection.CollectionSelectorViewModel.UiState.ShowConfirmDialog
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.widgets.view.CategoryChip
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.model.toSanitizedCollectionName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionSelectorActivity : DashlaneActivity() {

    val viewModel by viewModels<CollectionSelectorViewModel>()

    private val navArgs by navArgs<CollectionSelectorActivityArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CollectionSelector(viewModel)
                ConfirmDialog(viewModel)
            }
        }
    }

    @Composable
    fun ConfirmDialog(viewModel: CollectionSelectorViewModel) {
        val state by viewModel.uiState.collectAsState()
        val collection = (state as? ShowConfirmDialog)?.collection ?: return
        Dialog(
            onDismissRequest = { viewModel.cancelAddToSharedCollection() },
            title = stringResource(id = R.string.collection_detail_dialog_confirm_title),
            description = {
                Text(
                    text = stringResource(
                        id = R.string.collection_detail_dialog_confirm_text,
                        collection.name
                    )
                )
            },
            mainActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.collection_detail_dialog_confirm_positive_button)
            ),
            mainActionClick = { sendResult(collection) },
            additionalActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.collection_detail_dialog_confirm_negative_button)
            ),
            additionalActionClick = { viewModel.cancelAddToSharedCollection() }
        )
    }

    @Composable
    @Suppress("LongMethod")
    fun CollectionSelector(
        viewModel: CollectionSelectorViewModel
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.collection_selector_activity_title),
                            style = DashlaneTheme.typography.titleSectionMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            DeviceUtils.hideKeyboard(this@CollectionSelectorActivity)
                            finish()
                        }) {
                            Icon(
                                token = IconTokens.actionCloseOutlined,
                                contentDescription = stringResource(id = R.string.and_accessibility_close),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    backgroundColor = DashlaneTheme.colors.containerAgnosticNeutralStandard
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                val collections by viewModel.collections.collectAsState()
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = viewModel.userPrompt,
                    onValueChange = viewModel::updateUserPrompt,
                    label = stringResource(id = R.string.collection_search_field_label),
                    placeholder = stringResource(id = com.dashlane.ui.R.string.collection_search_field_placeholder),
                    actions = TextFieldActions.ClearField(contentDescription = stringResource(id = R.string.and_accessibility_action_text_clear)) {
                        viewModel.updateUserPrompt("")
                        true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (viewModel.canCreate) {
                        val label = viewModel.userPrompt.toSanitizedCollectionName()
                        val collection = Collection(name = label, shared = false)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onClickCollection(collection) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp, end = 8.dp)
                                ) {
                                    CategoryChip(label = collection.name) {
                                        onClickCollection(collection)
                                    }
                                }
                                Badge(
                                    modifier = Modifier.weight(1f, fill = false),
                                    text = stringResource(id = R.string.collection_selector_activity_new_badge),
                                    badgeIcon = BadgeIcon.Leading(IconTokens.actionAddOutlined),
                                    mood = Mood.Brand,
                                    intensity = Intensity.Supershy
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    items(collections) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClickCollection(it) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryChip(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                label = it.name,
                                shared = it.shared
                            ) {
                                onClickCollection(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickCollection(collection: Collection) {
        val sanitizedCollection = collection.copy(
            name = collection.name.toSanitizedCollectionName()
        )
        if (collection.shared) {
            viewModel.confirmAddToSharedCollection(sanitizedCollection)
            return
        }
        sendResult(sanitizedCollection)
    }

    private fun sendResult(collection: Collection) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(RESULT_TEMPORARY_COLLECTION, collection)
            }
        )
        DeviceUtils.hideKeyboard(this)
        finish()
    }

    override fun finish() {
        super.finish()
        if (!navArgs.fromView) {
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
    }

    companion object {
        const val SHOW_COLLECTION_SELECTOR = 3004
        const val RESULT_TEMPORARY_COLLECTION = "temporaryCollection"
    }
}