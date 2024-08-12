package com.dashlane.item.linkedwebsites

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dashlane.R
import com.dashlane.databinding.FragmentLinkedServicesBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.linkedwebsites.item.LinkedAppsItemFragment
import com.dashlane.item.linkedwebsites.item.LinkedAppsViewModel
import com.dashlane.item.linkedwebsites.item.LinkedWebsitesItemFragment
import com.dashlane.item.linkedwebsites.item.LinkedWebsitesViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.setCurrentPageView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
open class LinkedServicesFragment : Fragment() {

    private val viewModel by viewModels<LinkedServicesViewModel>()
    private val websitesViewModel by viewModels<LinkedWebsitesViewModel>()
    private val appsViewModel by viewModels<LinkedAppsViewModel>()

    private val menuProvider: MenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            createMenu(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == android.R.id.home) {
                onClose(true)
                return true
            }
            return false
        }
    }

    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.setCurrentPageView(AnyPage.ITEM_CREDENTIAL_DETAILS_WEBSITES)
        val binding = FragmentLinkedServicesBinding.inflate(layoutInflater)
        binding.viewpager.adapter = ListPagerAdapter(
            this,
            listOf(LinkedWebsitesItemFragment(), LinkedAppsItemFragment())
        )
        TabLayoutMediator(binding.servicesTabs, binding.viewpager) { tab, position ->
            val textResId = if (position == 0) {
                R.string.linked_services_websites_title
            } else {
                R.string.linked_services_apps_title
            }
            tab.text = getString(textResId)
        }.attach()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    it.vaultItem?.let { vaultItem ->
                        websitesViewModel.setupVaultItem(vaultItem)
                        appsViewModel.setupVaultItem(vaultItem)
                    }
                    websitesViewModel.websiteOpened()
                    if (it.actionClosePageAfterSave) {
                        setResultAndClose(temporarySaveData = false, isDataSaved = true)
                    }
                    if (it.closePageImmediate) {
                        setResultAndClose(temporarySaveData = false, isDataSaved = false)
                    }
                    websitesViewModel.changeEditMode(it.editMode)
                    appsViewModel.changeEditMode(it.editMode)
                }
            }
        }
        updateMenuProvider()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onClose(true)
                }
            }
        )
    }

    private fun updateMenuProvider() {
        requireActivity().removeMenuProvider(menuProvider)
        requireActivity().addMenuProvider(menuProvider)
    }

    fun createMenu(menu: Menu) {
        menu.clear()
        when {
            !viewModel.fromViewOnly -> {
                menu.add(R.string.multi_domain_credentials_cta_done).apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    setOnMenuItemClickListener {
                        onClose(false)
                        return@setOnMenuItemClickListener true
                    }
                }
            }
            viewModel.fromViewOnly && viewModel.isEditMode -> {
                menu.add(R.string.dashlane_save).apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    setIcon(R.drawable.save)
                    iconTintList = ColorStateList.valueOf(requireContext().getColor(R.color.text_neutral_standard))
                    setOnMenuItemClickListener {
                        onClose(false)
                        return@setOnMenuItemClickListener true
                    }
                }
            }
            !viewModel.isEditMode && viewModel.canEdit() -> {
                menu.add(R.string.edit).apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    setIcon(R.drawable.edit)
                    iconTintList = ColorStateList.valueOf(requireContext().getColor(R.color.text_neutral_standard))
                    setOnMenuItemClickListener {
                        viewModel.switchEditMode()
                        updateMenuProvider()
                        return@setOnMenuItemClickListener true
                    }
                }
            }
        }
    }

    private fun showDuplicateItem(duplicateUrl: String, itemName: String) {
        DialogHelper().builder(requireContext(), R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog_SmallTitle)
            .setTitle(getString(R.string.multi_domain_credentials_duplicate_title, duplicateUrl))
            .setMessage(getString(R.string.multi_domain_credentials_duplicate, itemName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.multi_domain_credentials_duplicate_positive) { _, _ ->
                saveChange()
            }.show()
    }

    private fun askForSave() {
        NotificationDialogFragment.Builder().setTitle(getString(R.string.save_item_))
            .setMessage(getString(R.string.would_you_like_to_save_the_item_))
            .setNegativeButtonText(getString(R.string.discart))
            .setPositiveButtonText(getString(R.string.dialog_save_item_save_button)).setCancelable(true)
            .setClickPositiveOnCancel(false).setClicker(object : NotificationDialogFragment.TwoButtonClicker {

                override fun onNegativeButton() {
                    setResultAndClose(temporarySaveData = false, isDataSaved = false)
                }

                override fun onPositiveButton() {
                    onClose(false)
                }
            }).build().show(requireActivity().supportFragmentManager, SAVE_DIALOG_TAG)
    }

    fun onClose(backPressed: Boolean) {
        
        if (viewModel.isEditMode) {
            if (hasDataToSave()) {
                
                if (backPressed && viewModel.fromViewOnly) {
                    askForSave()
                } else {
                    saveData()
                }
                return
            } else if (viewModel.fromViewOnly) {
                
                viewModel.switchEditMode()
                updateMenuProvider()
                return
            }
        }
        navigator.popBackStack()
    }

    private fun saveData() {
        val duplicateWebsite = viewModel.hasOtherItemsDuplicate(websitesViewModel.getEditableWebsitesResult())
        if (duplicateWebsite != null) {
            showDuplicateItem(duplicateWebsite.second, duplicateWebsite.first)
        } else {
            saveChange()
        }
    }

    private fun hasDataToSave() =
        viewModel.hasWebsitesToSave(websitesViewModel.getEditableWebsitesResult()) ||
            viewModel.hasAppsToSave(appsViewModel.getEditableAppsResult())

    private fun setResultAndClose(temporarySaveData: Boolean, isDataSaved: Boolean) {
        if (temporarySaveData) {
            setFragmentResult(
                RESULT_LINKED_SERVICES,
                Bundle().apply {
                    if (viewModel.hasWebsitesToSave(websitesViewModel.getEditableWebsitesResult())) {
                        putStringArray(
                            RESULT_TEMPORARY_WEBSITES,
                            websitesViewModel.getEditableWebsitesResult().toTypedArray()
                        )
                    }
                    if (viewModel.hasAppsToSave(appsViewModel.getEditableAppsResult())) {
                        putStringArray(
                            RESULT_TEMPORARY_APPS,
                            appsViewModel.getEditableAppsResult().toTypedArray()
                        )
                    }
                }
            )
        }
        if (isDataSaved) {
            setFragmentResult(
                RESULT_LINKED_SERVICES,
                Bundle().apply {
                    putBoolean(RESULT_DATA_SAVED, true)
                }
            )
        }
        findNavController().popBackStack()
    }

    private fun saveChange() {
        if (viewModel.fromViewOnly) {
            viewModel.save(websitesViewModel.getEditableWebsitesResult(), appsViewModel.getEditableAppsResult())
        } else {
            setResultAndClose(temporarySaveData = true, isDataSaved = false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity().removeMenuProvider(menuProvider)
    }

    private class ListPagerAdapter(
        parentFragment: Fragment,
        private val linkedServicesFragments: List<Fragment>
    ) : FragmentStateAdapter(parentFragment) {

        override fun getItemCount(): Int = linkedServicesFragments.size

        override fun createFragment(position: Int): Fragment = linkedServicesFragments[position]
    }

    companion object {
        const val RESULT_LINKED_SERVICES = "resultLinkedServices"
        const val RESULT_TEMPORARY_WEBSITES = "resultTemporaryWebsite"
        const val RESULT_TEMPORARY_APPS = "resultTemporaryApps"
        const val RESULT_DATA_SAVED = "resultDataSaved"
        const val SAVE_DIALOG_TAG = "save_dialog"
    }
}