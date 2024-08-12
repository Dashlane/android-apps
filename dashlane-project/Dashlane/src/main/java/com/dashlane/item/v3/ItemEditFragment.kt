package com.dashlane.item.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.authenticator.suggestions.AuthenticatorIntroResult
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.collection.CollectionSelectorFragment
import com.dashlane.item.delete.DeleteVaultItemFragment
import com.dashlane.item.linkedwebsites.LinkedServicesFragment
import com.dashlane.item.passwordhistory.PasswordHistoryFragment
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.v3.data.toCollectionData
import com.dashlane.item.v3.display.ItemEditScreen
import com.dashlane.item.v3.display.Toolbar
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.util.getParcelableCompat
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject

@AndroidEntryPoint
class ItemEditFragment : Fragment() {
    val viewModel: ItemEditViewModel by viewModels(factoryProducer = { ItemEditViewModelFactory(this) })
    private val actionsMenu = mutableMapOf<Action, MenuItem>()
    private lateinit var actionBarView: ComposeView
    private lateinit var resultLauncherAuthenticator: ActivityResultLauncher<CredentialItem>

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var frozenStateManager: FrozenStateManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        actionBarView = ComposeView(requireContext())
        val appCompatActivity = requireActivity() as AppCompatActivity
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
                    val state = rememberLazyListState()
                    Toolbar(
                        actionBar = appCompatActivity.supportActionBar,
                        lazyListState = state,
                        uiState = uiState.value,
                        actionBarView = actionBarView
                    )
                    ItemEditScreen(
                        viewModel = viewModel,
                        uiState = uiState.value,
                        lazyListState = state,
                        activity = appCompatActivity,
                        navigator = navigator,
                        viewLifecycleOwner = viewLifecycleOwner,
                        clock = clock,
                        resultLauncherAuthenticator = resultLauncherAuthenticator,
                        actionsMenu = actionsMenu,
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupResultListeners()
    }

    override fun onResume() {
        super.onResume()
        
        
        (viewModel as CredentialItemEditViewModel).handleSharingResult()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (actionBarView.parent as? ViewGroup)?.removeView(actionBarView)
    }

    private fun setupResultListeners() {
        
        setFragmentResultListener(CollectionSelectorFragment.RESULT_COLLECTION_SELECTOR) { _, bundle ->
            val selectedCollection =
                bundle.getParcelableCompat<ItemCollectionListSubView.Collection>(
                    CollectionSelectorFragment.RESULT_TEMPORARY_COLLECTION
                )
            selectedCollection?.toCollectionData()?.let {
                (viewModel as CredentialItemEditViewModel).addCollection(it)
            }
        }
        
        setFragmentResultListener(LinkedServicesFragment.RESULT_LINKED_SERVICES) { _, bundle ->
            if (bundle.containsKey(LinkedServicesFragment.RESULT_DATA_SAVED)) {
                (viewModel as CredentialItemEditViewModel).reloadLinkedServices()
                return@setFragmentResultListener
            }
            val temporaryWebsites =
                bundle.getStringArray(LinkedServicesFragment.RESULT_TEMPORARY_WEBSITES)
            val temporaryApps = bundle.getStringArray(LinkedServicesFragment.RESULT_TEMPORARY_APPS)
            (viewModel as CredentialItemEditViewModel).updateLinkedServices(
                temporaryWebsites?.toList(),
                temporaryApps?.toList()
            )
        }

        
        setFragmentResultListener(DeleteVaultItemFragment.DELETE_VAULT_ITEM_RESULT) { _, bundle ->
            val isItemDeleted = bundle.getBoolean(DeleteVaultItemFragment.DELETE_VAULT_ITEM_SUCCESS)
            if (!isItemDeleted) return@setFragmentResultListener
            val animTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            viewModel.closeAfterRemoved(animTime)
        }

        
        setFragmentResultListener(PasswordHistoryFragment.PASSWORD_HISTORY_RESULT) { _, bundle ->
            val hasError = bundle.getBoolean(PasswordHistoryFragment.FINISHED_WITH_ERROR_EXTRA)
            (viewModel as CredentialItemEditViewModel).handlePasswordRestoreResult(!hasError)
        }

        
        requireActivity().supportFragmentManager.setFragmentResultListener(
            PasswordGeneratorDialog.PASSWORD_GENERATOR_REQUEST_KEY,
            requireActivity()
        ) { _, bundle ->
            val id = bundle.getString(PasswordGeneratorDialog.PASSWORD_GENERATOR_RESULT_ID)
            val password =
                bundle.getString(PasswordGeneratorDialog.PASSWORD_GENERATOR_RESULT_PASSWORD)
                    ?: return@setFragmentResultListener
            (viewModel as CredentialItemEditViewModel).updatePassword(password, id)
            viewModel.actionHandled()
        }

        
        resultLauncherAuthenticator =
            registerForActivityResult(AuthenticatorIntroResult()) { (_, otp) ->
                otp?.let { (viewModel as CredentialItemEditViewModel).update2FA(it) }
            }
    }
}