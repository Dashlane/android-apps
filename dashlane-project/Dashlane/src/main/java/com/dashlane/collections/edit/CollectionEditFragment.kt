package com.dashlane.collections.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.vault.model.toSanitizedCollectionName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class CollectionEditFragment : AbstractContentFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    CollectionEditScreen()
                }
            }
        }
    }

    @Composable
    fun CollectionEditScreen(viewModel: CollectionsEditViewModel = viewModel()) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val editMode = uiState.viewData.editMode
        LaunchedEffect(key1 = editMode) {
            (activity as? DashlaneActivity)?.supportActionBar?.apply {
                title = resources.getString(
                    if (editMode) {
                        R.string.collection_edit_title
                    } else {
                        R.string.collection_add_title
                    }
                )
            }
        }

        when (uiState) {
            is ViewState.Form,
            is ViewState.Error -> {
                CollectionEditForm(uiState, viewModel)
            }

            is ViewState.Loading -> {
                
            }

            is ViewState.Saved -> {
                navigator.popBackStack()
            }
        }
    }

    @Composable
    private fun CollectionEditForm(
        uiState: ViewState,
        viewModel: CollectionsEditViewModel
    ) {
        val menuProvider = remember {
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.save_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.menu_save) {
                        viewModel.saveClicked()
                        return true
                    }
                    return false
                }
            }
        }
        val isNameFilled = uiState.viewData.collectionName.text.toSanitizedCollectionName().isNotEmpty()
        LaunchedEffect(isNameFilled) {
            (activity as? DashlaneActivity)?.run {
                if (isNameFilled) {
                    addMenuProvider(menuProvider, viewLifecycleOwner)
                } else {
                    removeMenuProvider(menuProvider)
                }
            }
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(key1 = focusRequester) {
                delay(200) 
                focusRequester.requestFocus()
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = uiState.viewData.collectionName,
                onValueChange = viewModel::onNameChanged,
                label = stringResource(id = R.string.collection_add_collection_name_field_label),
                isError = uiState is ViewState.Error,
                feedbackText = getErrorMessageResId((uiState as? ViewState.Error)?.errorType)?.let {
                    stringResource(id = it)
                }
            )
            val spaces = uiState.viewData.availableSpaces
            val selectedSpace = uiState.viewData.space
            if (spaces != null && selectedSpace != null) {
                Spacer(modifier = Modifier.height(24.dp))
                SpacePicker(
                    spaces = spaces,
                    onSpaceSelected = viewModel::onSpaceSelected,
                    selectedSpace = selectedSpace,
                    enabled = !uiState.viewData.editMode 
                )
            }
        }
    }

    @StringRes
    private fun getErrorMessageResId(errorType: ErrorType?): Int? = when (errorType) {
        ErrorType.COLLECTION_ALREADY_EXISTS -> R.string.collection_add_error_collection_already_exist
        ErrorType.EMPTY_NAME -> null
        else -> null
    }
}