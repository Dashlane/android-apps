package com.dashlane.ui.activities.fragments.checklist

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.databinding.FragmentChecklistBinding
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.checklist.ChecklistViewSetup.setupView
import kotlinx.coroutines.launch

class ChecklistViewProxy(
    private val viewModel: ChecklistViewModelContract,
    private val binding: FragmentChecklistBinding,
    private val lifecycle: Lifecycle,
    private val navigator: Navigator,
    private val m2wResultLauncher: ActivityResultLauncher<Unit>
) {
    private val context
        get() = binding.root.context

    init {
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.checkListDataFlow.collect {
                    setChecklist(it)
                }
            }
        }
    }

    private fun setChecklist(checklistData: ChecklistData?) {
        setupView(
            getStartedBinding = binding.checklistGroup,
            item = checklistData,
            onDismiss = ::onDismiss,
            onImportPasswords = { navigator.goToCredentialAddStep1(expandImportOptions = true) },
            onActivateAutofill = { navigator.goToInAppLoginIntro() },
            onShowM2d = {
                m2wResultLauncher.launch()
            },
            onShowDarkWebMonitoring = {
                viewModel.onDWMViewScanResult()
                navigator.goToDarkWebMonitoring()
            }
        )
    }

    private fun onDismiss() {
        viewModel.onDismissChecklistClicked()
    }
}