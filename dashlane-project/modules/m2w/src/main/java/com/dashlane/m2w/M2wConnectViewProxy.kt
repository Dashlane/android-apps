package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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
        rootView.findViewById<Button>(R.id.view_next)!!.setOnClickListener {
            viewModel.onNext()
        }
        ViewCompat.setAccessibilityHeading(rootView.findViewById(R.id.view_m2w_url_prompt)!!, true)

        lifecycleScope.launch {
            viewModel.showConfirmPopupFlow.collectLatest { show ->
                if (show) showConfirmPopup()
            }
        }

        lifecycleScope.launch {
            viewModel.finishM2W.collect { success ->
                val data = Intent().apply {
                    putExtra(M2wIntroActivity.EXTRA_FINISH, true)
                    putExtra(M2wIntentCoordinator.EXTRA_M2W_COMPLETED, success)
                }
                activity.setResult(Activity.RESULT_OK, data)
                activity.finish()
            }
        }
    }

    fun showConfirmPopup() {
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