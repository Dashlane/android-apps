package com.dashlane.ui.screens.sharing

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.coroutineScope
import com.dashlane.R
import com.dashlane.core.domain.sharing.SharingPermission
import com.dashlane.core.domain.sharing.toSharingPermission
import com.dashlane.core.domain.sharing.toUserPermission
import com.dashlane.ui.adapters.sharing.SharingContactFilteredArrayAdapter
import com.dashlane.ui.dialogs.fragment.WaiterDialogFragment
import com.dashlane.ui.screens.fragments.sharing.dialog.SharingPermissionInfoDialogFragment
import com.dashlane.ui.screens.fragments.sharing.dialog.SharingPermissionSelectionDialogFragment
import com.dashlane.ui.util.DialogHelper
import com.dashlane.ui.widgets.view.SharingContactAutocompleteTextView
import com.dashlane.util.getSerializableCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NewSharePeopleViewProxy(
    private val fragment: Fragment,
    view: View,
    private val viewModel: NewSharePeopleViewModelContract,
) {
    private val coroutineScope: CoroutineScope
        get() = fragment.lifecycle.coroutineScope

    private val fragmentManager: FragmentManager
        get() = fragment.parentFragmentManager

    private val context: Context
        get() = fragment.requireContext()

    private val emailAddresses: SharingContactAutocompleteTextView =
        view.findViewById(R.id.email_addresses_tokens)

    init {
        val ctaChangePermission = view.findViewById<TextView>(R.id.permission_change)
        val permissionTextView = view.findViewById<TextView>(R.id.permission_value)
        val permissionInfo = view.findViewById<ImageButton>(R.id.permission_info)
        ctaChangePermission.setOnClickListener {
            showPermissionDialog(true)
        }
        permissionTextView.setOnClickListener {
            showPermissionDialog(true)
        }
        permissionInfo.setOnClickListener {
            showPermissionInfoDialog()
        }

        coroutineScope.launch {
            viewModel.contacts.collect {
                setup(it)
            }
        }
        coroutineScope.launch {
            viewModel.permission.collect {
                permissionTextView.setText(it.toSharingPermission().stringResource)
            }
        }
        coroutineScope.launch {
            viewModel.uiState.collect {
                when (it) {
                    NewSharePeopleViewModelContract.UIState.LOADING -> showLoadingDialog(true)
                    NewSharePeopleViewModelContract.UIState.SUCCESS -> onSuccess()
                    NewSharePeopleViewModelContract.UIState.SUCCESS_FOR_RESULT ->
                        onSuccessForResult()
                    NewSharePeopleViewModelContract.UIState.ERROR ->
                        showErrorDialog(
                            R.string.sharing_dialog_error_premium_status_undefined_description
                        )
                    NewSharePeopleViewModelContract.UIState.ERROR_FIND_USERS ->
                        showErrorDialog(
                            R.string.ui_sharing_error_network_or_server_error
                        )
                    NewSharePeopleViewModelContract.UIState.ERROR_ALREADY_ACCESS ->
                        showErrorDialog(
                            R.string.ui_sharing_error_cannot_share_with_only_you
                        )
                    else -> showLoadingDialog(false)
                }
            }
        }

        fragment.setFragmentResultListener(CHANGE_PERMISSION_REQUEST_KEY) { _, bundle ->
            showPermissionDialog(false)
            val permission = bundle.getSerializableCompat<SharingPermission>(CHANGE_PERMISSION_VALUE)
                ?: return@setFragmentResultListener
            viewModel.onPermissionChanged(permission.toUserPermission())
        }
    }

    private fun onSuccessForResult() {
       fragment.activity?.apply {
           setResult(Activity.RESULT_OK)
           finish()
       }
    }

    fun onClickShare() {
        emailAddresses.performCompletion()
        viewModel.onClickShare(emailAddresses.objects)
    }

    private fun setup(data: List<SharingContact>) {
        val adapter = SharingContactFilteredArrayAdapter(
            context,
            R.layout.list_item_sharing_contact_chips,
            data
        )
        emailAddresses.setAdapter(adapter)
    }

    private fun showPermissionDialog(show: Boolean) {
        (fragmentManager.findFragmentByTag(SharingPermissionSelectionDialogFragment.TAG) as? DialogFragment)?.dismissAllowingStateLoss()
        if (show) {
            SharingPermissionSelectionDialogFragment.newInstance(
                context,
                viewModel.permission.value.toSharingPermission()
            ).show(fragmentManager, SharingPermissionSelectionDialogFragment.TAG)
        }
    }

    private fun showPermissionInfoDialog() {
        (fragmentManager.findFragmentByTag(SharingPermissionInfoDialogFragment.TAG) as? DialogFragment)?.dismissAllowingStateLoss()
        SharingPermissionInfoDialogFragment.newInstance(context)
            .show(fragmentManager, SharingPermissionInfoDialogFragment.TAG)
    }

    private fun showLoadingDialog(show: Boolean) {
        WaiterDialogFragment.dismissWaiter(fragmentManager)
        if (show) {
            WaiterDialogFragment.showWaiter(
                false,
                context.getString(R.string.contacting_dashlane),
                context.getString(R.string.contacting_dashlane),
                fragmentManager
            )
        }
    }

    private fun onSuccess() {
        WaiterDialogFragment.dismissWaiter(fragmentManager)
    }

    private fun showErrorDialog(@StringRes descriptionResId: Int) {
        WaiterDialogFragment.dismissWaiter(fragmentManager)
        DialogHelper()
            .builder(context)
            .setMessage(context.getString(descriptionResId))
            .setCancelable(true)
            .show()
    }

    companion object {
        const val CHANGE_PERMISSION_REQUEST_KEY = "change_permission_request_key"
        const val CHANGE_PERMISSION_VALUE = "change_permission_value"
    }
}
