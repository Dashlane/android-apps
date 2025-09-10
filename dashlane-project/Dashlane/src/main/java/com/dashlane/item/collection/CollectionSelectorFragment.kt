package com.dashlane.item.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionSelectorFragment : Fragment() {
    val viewModel by viewModels<CollectionSelectorViewModel>()

    private val menuProvider = object : MenuProvider {
        lateinit var saveMenuItem: MenuItem

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            saveMenuItem = menu.add(R.string.dashlane_save).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setIcon(R.drawable.save)
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == android.R.id.home) {
                viewModel.onBackPressed()
                return true
            } else if (menuItem == saveMenuItem) {
                viewModel.save()
                return true
            }
            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                }
            },
        )
        (requireActivity() as AppCompatActivity).addMenuProvider(menuProvider)
        viewModel.onViewStarted()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    LaunchedEffect(key1 = viewModel) {
                        viewModel.uiState.sideEffect.collect { sideEffect ->
                            when (sideEffect) {
                                is CollectionSelectorState.SideEffect.Close -> sendResult(sideEffect.hasSaved)
                            }
                        }
                    }
                    CollectionSelectorScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).removeMenuProvider(menuProvider)
    }

    private fun sendResult(hasSaved: Boolean) {
        setFragmentResult(
            RESULT_COLLECTION_SELECTOR,
            bundleOf(RESULT_COLLECTION_SELECTOR_CHANGE_SAVED to hasSaved),
        )
        DeviceUtils.hideKeyboard(requireActivity())
        findNavController().popBackStack()
    }

    companion object {
        const val RESULT_COLLECTION_SELECTOR = "collectionSelector"
        const val RESULT_COLLECTION_SELECTOR_CHANGE_SAVED = "collectionSelectorChangeSaved"
    }
}