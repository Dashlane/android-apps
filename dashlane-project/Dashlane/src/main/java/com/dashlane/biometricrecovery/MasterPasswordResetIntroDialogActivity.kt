package com.dashlane.biometricrecovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dashlane.R
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockPrompt
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.showToaster
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MasterPasswordResetIntroDialogActivity : DashlaneActivity() {

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope

    @Inject
    @MainCoroutineDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    lateinit var biometricRecovery: BiometricRecovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricRecovery.isFeatureKnown = true

        showBottomSheet()
    }

    private fun showBottomSheet() = BottomSheetDialog(this).apply {
        setContentView(R.layout.dialog_biometric_recovery_intro)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        findViewById<View>(R.id.positive_cta)?.setOnClickListener {
            applicationCoroutineScope.launch(mainDispatcher) {
                lockHelper.showAndWaitLockActivityForReason(
                    context = this@MasterPasswordResetIntroDialogActivity,
                    reason = LockEvent.Unlock.Reason.WithCode(UNLOCK_EVENT_CODE),
                    lockPrompt = LockPrompt.ForSettings,
                ).takeIf { lockEvent ->
                    lockEvent is LockEvent.Unlock &&
                        lockEvent.reason is LockEvent.Unlock.Reason.WithCode &&
                        (lockEvent.reason as LockEvent.Unlock.Reason.WithCode).requestCode == UNLOCK_EVENT_CODE
                }?.let {
                    biometricRecovery.setBiometricRecoveryFeatureEnabled(true)

                    this@MasterPasswordResetIntroDialogActivity.showToaster(
                        R.string.account_recovery_intro_dialog_success_message,
                        Toast.LENGTH_SHORT
                    )
                    cancel()
                }
            }
        }

        findViewById<View>(R.id.negative_cta)?.setOnClickListener { cancel() }

        setOnShowListener {
            
            findViewById<View>(R.id.design_bottom_sheet)?.let { BottomSheetBehavior.from(it).peekHeight = it.height }
        }

        setOnCancelListener {
            finish()
        }
    }.show()

    companion object {
        private const val UNLOCK_EVENT_CODE = 8747

        fun newIntent(context: Context) = Intent(context, MasterPasswordResetIntroDialogActivity::class.java)
    }
}
