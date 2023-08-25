package com.dashlane.item.linkedwebsites

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dashlane.R
import com.dashlane.databinding.ActivityLinkedWebsitesBinding
import com.dashlane.item.linkedwebsites.item.LinkedAppsItemFragment
import com.dashlane.item.linkedwebsites.item.LinkedAppsViewModel
import com.dashlane.item.linkedwebsites.item.LinkedWebsitesItemFragment
import com.dashlane.item.linkedwebsites.item.LinkedWebsitesViewModel
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.util.DialogHelper
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class LinkedServicesViewProxy(
    private val activity: AppCompatActivity,
    private val viewModel: LinkedServicesViewModel,
    private val websitesViewModel: LinkedWebsitesViewModel,
    private val appsViewModel: LinkedAppsViewModel,
    binding: ActivityLinkedWebsitesBinding
) {

    init {
        binding.run {
            viewpager.adapter = ListPagerAdapter(
                activity.supportFragmentManager,
                activity.lifecycle,
                listOf(LinkedWebsitesItemFragment(), LinkedAppsItemFragment())
            )
            TabLayoutMediator(servicesTabs, viewpager) { tab, position ->
                val textResId = if (position == 0) {
                    R.string.linked_services_websites_title
                } else {
                    R.string.linked_services_apps_title
                }
                tab.text = activity.getString(textResId)
            }.attach()
        }

        activity.run {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.state.collect {
                        it.vaultItem?.let { vaultItem ->
                            websitesViewModel.setupVaultItem(vaultItem)
                            appsViewModel.setupVaultItem(vaultItem)
                        }
                        websitesViewModel.websiteOpened()
                        if (it.actionClosePageAfterSave) {
                            setResult(LinkedServicesActivity.RESULT_DATA_SAVED)
                            finishActivity()
                        }
                        if (it.closePageImmediate) {
                            finishActivity()
                        }
                        websitesViewModel.changeEditMode(it.editMode)
                        appsViewModel.changeEditMode(it.editMode)
                    }
                }
            }
        }
    }

    fun createMenu(menu: Menu) {
        menu.clear()
        if (!viewModel.fromViewOnly) {
            menu.add(R.string.multi_domain_credentials_cta_done).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnMenuItemClickListener {
                    onClose(false)
                    return@setOnMenuItemClickListener true
                }
            }
        } else if (viewModel.fromViewOnly && viewModel.isEditMode) {
            menu.add(R.string.dashlane_save).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setIcon(R.drawable.save)
                iconTintList = ColorStateList.valueOf(activity.getColor(R.color.text_neutral_standard))
                setOnMenuItemClickListener {
                    onClose(false)
                    return@setOnMenuItemClickListener true
                }
            }
        } else if (!viewModel.isEditMode && viewModel.canEdit()) {
            menu.add(R.string.edit).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setIcon(R.drawable.edit)
                iconTintList = ColorStateList.valueOf(activity.getColor(R.color.text_neutral_standard))
                setOnMenuItemClickListener {
                    viewModel.switchEditMode()
                    activity.invalidateOptionsMenu()
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun temporarySaveMutableServices() {
        activity.setResult(
            Activity.RESULT_OK,
            Intent().apply {
            if (viewModel.hasWebsitesToSave(websitesViewModel.getEditableWebsitesResult())) {
                putExtra(
                    LinkedServicesActivity.RESULT_TEMPORARY_WEBSITES,
                    websitesViewModel.getEditableWebsitesResult().toTypedArray()
                )
            }
            if (viewModel.hasAppsToSave(appsViewModel.getEditableAppsResult())) {
                putExtra(
                    LinkedServicesActivity.RESULT_TEMPORARY_APPS,
                    appsViewModel.getEditableAppsResult().toTypedArray()
                )
            }
        }
        )
    }

    private fun showDuplicateItem(duplicateUrl: String, itemName: String) {
        DialogHelper().builder(activity, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog_SmallTitle)
            .setTitle(activity.getString(R.string.multi_domain_credentials_duplicate_title, duplicateUrl))
            .setMessage(activity.getString(R.string.multi_domain_credentials_duplicate, itemName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.multi_domain_credentials_duplicate_positive) { _, _ ->
                saveChange()
            }.show()
    }

    private fun askForSave() {
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.save_item_))
            .setMessage(activity.getString(R.string.would_you_like_to_save_the_item_))
            .setNegativeButtonText(activity.getString(R.string.discart))
            .setPositiveButtonText(activity.getString(R.string.dialog_save_item_save_button)).setCancelable(true)
            .setClickPositiveOnCancel(false).setClicker(object : NotificationDialogFragment.TwoButtonClicker {

                override fun onNegativeButton() {
                    finishActivity()
                }

                override fun onPositiveButton() {
                    onClose(false)
                }
            }).build().show(activity.supportFragmentManager, SAVE_DIALOG_TAG)
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
                activity.invalidateOptionsMenu()
                return
            }
        }
        finishActivity()
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

    private fun finishActivity() {
        activity.finish()
        if (!viewModel.fromViewOnly) {
            activity.overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
    }

    private fun saveChange() {
        if (viewModel.fromViewOnly) {
            viewModel.save(websitesViewModel.getEditableWebsitesResult(), appsViewModel.getEditableAppsResult())
        } else {
            temporarySaveMutableServices()
            finishActivity()
        }
    }

    private class ListPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val linkedServicesFragments: List<Fragment>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = linkedServicesFragments.size

        override fun createFragment(position: Int): Fragment = linkedServicesFragments[position]
    }

    companion object {
        const val SAVE_DIALOG_TAG = "save_dialog"
    }
}