package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.dashlane.design.component.compat.view.ButtonMediumView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class M2wConnectViewProxy(
    private val activity: AppCompatActivity,
    val viewModel: M2wConnectViewModelContract,
    val lifecycleScope: CoroutineScope
) {

    private val rootView: View = activity.findViewById(android.R.id.content)

    init {
        rootView.findViewById<ButtonMediumView>(R.id.view_next)!!.onClick = {
            viewModel.onNext()
        }
        rootView.findViewById<ButtonMediumView>(R.id.button_skip)!!.onClick = {
            viewModel.onSkip()
        }
        ViewCompat.setAccessibilityHeading(rootView.findViewById(R.id.view_m2w_url_prompt)!!, true)

        lifecycleScope.launch {
            viewModel.showConfirmPopupFlow.collectLatest { show ->
                if (show) showConfirmPopup()
            }
        }

        lifecycleScope.launch {
            viewModel.finishM2W.collect { result ->
                val data = Intent()
                data.putExtra(M2wIntroActivity.EXTRA_FINISH, true)
                when (result) {
                    M2WResult.SKIPPED -> data.putExtra(
                        M2wIntentCoordinator.EXTRA_M2W_SKIPPED,
                        true
                    )
                    M2WResult.COMPLETED -> data.putExtra(
                        M2wIntentCoordinator.EXTRA_M2W_COMPLETED,
                        true
                    )
                    else -> Unit
                }
                activity.setResult(Activity.RESULT_OK, data)
                activity.finish()
            }
        }
    }

    private fun showConfirmPopup() {
        AlertDialog.Builder(rootView.context).setMessage(R.string.m2w_connect_confirm_message)
            .setPositiveButton(R.string.m2w_connect_confirm_cta_positive) { _, _ ->
                viewModel.onConfirmSuccess()
            }
            .setNegativeButton(R.string.m2w_connect_confirm_cta_negative) { _, _ ->
                viewModel.onCancelConfirmPopup()
            }
            .setOnDismissListener { viewModel.onConfirmationDialogDismissed() }
            .show()
    }
}