package com.dashlane.item.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dashlane.R
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BadgeIcon
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.DialogActionType
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.collection.CollectionSelectorViewModel.UiState.ShowConfirmDialog
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.ui.widgets.view.CategoryChip
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.model.toSanitizedCollectionName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionSelectorFragment : Fragment() {

    val viewModel by viewModels<CollectionSelectorViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    DeviceUtils.hideKeyboard(requireActivity())
                    findNavController().popBackStack()
                }
            }
        )
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_up_indicator_close)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    
                    val collections by viewModel.collections.collectAsState()
                    CollectionSelector(
                        collections,
                        viewModel.canCreate,
                        viewModel.userPrompt,
                        viewModel::updateUserPrompt
                    )
                    ConfirmDialog(viewModel)
                }
            }
        }
    }

    @Composable
    fun ConfirmDialog(viewModel: CollectionSelectorViewModel) {
        val state by viewModel.uiState.collectAsState()
        val collection = (state as? ShowConfirmDialog)?.collection ?: return
        val isLimited = (state as? ShowConfirmDialog)?.isLimited ?: false
        if (isLimited) {
            Dialog(
                onDismissRequest = { viewModel.cancelAddToSharedCollection() },
                title = stringResource(id = R.string.collection_detail_dialog_limited_title),
                description = {
                    Text(
                        text = stringResource(id = R.string.collection_detail_dialog_limited_description)
                    )
                },
                mainActionLayout = ButtonLayout.TextOnly(
                    stringResource(id = R.string.collection_detail_dialog_confirm_negative_button)
                ),
                mainActionClick = { viewModel.cancelAddToSharedCollection() },
                mainActionType = DialogActionType.SECONDARY
            )
        } else {
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
    }

    @Composable
    @Suppress("LongMethod")
    fun CollectionSelector(
        collections: List<Collection>,
        canCreate: Boolean,
        userPrompt: String,
        onUpdateUserPrompt: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val focusRequester = remember { FocusRequester() }
            TextField(
                value = userPrompt,
                onValueChange = onUpdateUserPrompt,
                label = stringResource(id = R.string.collection_search_field_label),
                placeholder = stringResource(id = com.dashlane.ui.R.string.collection_search_field_placeholder),
                actions = TextFieldActions.ClearField(contentDescription = stringResource(id = R.string.and_accessibility_action_text_clear)) {
                    onUpdateUserPrompt("")
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
                if (canCreate) {
                    val label = userPrompt.toSanitizedCollectionName()
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

    @Composable
    @Preview
    private fun CollectionSelectorPreview() {
        DashlaneTheme {
            CollectionSelector(
                collections = listOf(
                    Collection(name = "MyFirst", shared = false),
                    Collection(name = "Second", shared = false),
                    Collection(name = "Friends", shared = true)
                ),
                canCreate = true,
                userPrompt = "Looking for a collection?",
                onUpdateUserPrompt = { }
            )
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
        setFragmentResult(
            RESULT_COLLECTION_SELECTOR,
            bundleOf(RESULT_TEMPORARY_COLLECTION to collection)
        )
        DeviceUtils.hideKeyboard(requireActivity())
        findNavController().popBackStack()
    }

    companion object {
        const val RESULT_COLLECTION_SELECTOR = "collectionSelector"
        const val RESULT_TEMPORARY_COLLECTION = "temporaryCollection"
    }
}