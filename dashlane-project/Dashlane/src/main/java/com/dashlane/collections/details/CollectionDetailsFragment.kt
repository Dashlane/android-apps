package com.dashlane.collections.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.SnackbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionDetailsFragment : AbstractContentFragment() {

    private lateinit var actionBarView: ComposeView
    private val viewModel: CollectionDetailsViewModel by viewModels()
    private var previousMenuProvider: MenuProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        actionBarView = ComposeView(requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    SetupToolbar(
                        editAllowed = uiState.viewData.editAllowed,
                        deleteAllowed = uiState.viewData.deleteAllowed,
                        isBusiness = uiState.viewData.spaceData?.businessSpace ?: false
                    )
                    CollectionDetailsScreen(
                        uiState = uiState,
                        onDismissDialog = { viewModel.dismissDialogClicked() },
                        onDeleteConfirmed = { viewModel.confirmDeleteClicked(uiState.viewData.shared) },
                        onRevokeToDeleteClicked = ::navigateToSharedAccess,
                        onBack = { navigator.popBackStack() },
                        goToItem = { id, type -> navigator.goToItem(uid = id, type = type) },
                        removeItem = viewModel::removeItem
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.mayRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (actionBarView.parent as? ViewGroup)?.removeView(actionBarView)
    }

    @Composable
    fun SetupToolbar(editAllowed: Boolean, deleteAllowed: Boolean, isBusiness: Boolean) {
        var displayBusinessMemberLimitDialog by rememberSaveable { mutableStateOf(false) }
        val menuHost: MenuHost = requireActivity()
        val disabledTextColor = DashlaneTheme.colors.textOddityDisabled.value
        val menuProvider = remember(editAllowed, deleteAllowed) {
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.collection_detail_menu, menu)
                    menu.findItem(R.id.menu_share).isVisible = isBusiness
                    menu.findItem(R.id.menu_shared_access).isVisible = isBusiness && viewModel.navArgs.sharedCollection
                    menu.findItem(R.id.menu_share).updateStateAndTitleColor(
                        viewModel.navArgs.shareAllowed && !viewModel.isCollectionSharingLimited,
                        disabledTextColor
                    )
                    menu.findItem(R.id.menu_edit).updateStateAndTitleColor(
                        editAllowed,
                        disabledTextColor
                    )
                    menu.findItem(R.id.menu_delete).updateStateAndTitleColor(
                        deleteAllowed,
                        disabledTextColor
                    )
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menu_delete -> {
                            viewModel.listeningChanges = true
                            viewModel.deleteClicked()
                            true
                        }
                        R.id.menu_edit -> {
                            viewModel.listeningChanges = true
                            navigator.goToCollectionEditFromCollectionDetail(
                                collectionId = viewModel.navArgs.collectionId,
                                sharedCollection = viewModel.navArgs.sharedCollection
                            )
                            true
                        }
                        R.id.menu_share -> {
                            if (viewModel.uiState.value.viewData.collectionLimit == CollectionLimiter.UserLimit.NOT_ADMIN) {
                                displayBusinessMemberLimitDialog = true
                            } else if (viewModel.uiState.value.viewData.hasItemWithAttachment) {
                                viewModel.attemptSharingWithAttachment()
                            } else {
                                
                                
                                viewModel.listeningChanges = !viewModel.navArgs.sharedCollection
                                navigator.goToCollectionShareFromCollectionDetail(viewModel.navArgs.collectionId)
                            }
                            true
                        }
                        R.id.menu_shared_access -> {
                            navigateToSharedAccess()
                            true
                        }
                        else -> false
                    }
                }
            }
        }
        LaunchedEffect(key1 = menuProvider) {
            previousMenuProvider?.let {
                menuHost.removeMenuProvider(it)
                previousMenuProvider = null
            }
            menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
            previousMenuProvider = menuProvider
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(key1 = uiState) {
            if (uiState is ViewState.SharingWithAttachmentError) {
                activity?.let { activity ->
                    SnackbarUtils.showSnackbar(
                        activity,
                        activity.getString(R.string.collection_error_sharing_collection_containing_items_with_attachments)
                    )
                }
            }
        }
        actionBarView.setContent { CollectionDetailScreenToolbar(uiState) }
        if (actionBarView.parent == null) {
            (activity as AppCompatActivity).supportActionBar?.customView = actionBarView
        }
        if (displayBusinessMemberLimitDialog) {
            DialogBusinessMemberLimit {
                displayBusinessMemberLimitDialog = false
            }
        }
    }

    private fun navigateToSharedAccess() {
        viewModel.listeningChanges = true
        viewModel.userAccessCouldChange = true
        navigator.goToCollectionSharedAccessFromCollectionDetail(viewModel.navArgs.collectionId)
    }
}