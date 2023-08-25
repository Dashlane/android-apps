package com.dashlane.biometricrecovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dashlane.R
import com.dashlane.lock.LockHelper.Companion.PROMPT_LOCK_FOR_SETTINGS
import com.dashlane.lock.UnlockEvent
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.showToaster
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
                    this@MasterPasswordResetIntroDialogActivity,
                    UnlockEvent.Reason.WithCode(UNLOCK_EVENT_CODE),
                    PROMPT_LOCK_FOR_SETTINGS,
                    getString(R.string.please_enter_master_password_to_edit_settings)
                )?.takeIf { unlockEvent ->
                    val reason = unlockEvent.reason
                    unlockEvent.isSuccess() &&
                        reason is UnlockEvent.Reason.WithCode &&
                        reason.requestCode == UNLOCK_EVENT_CODE
                }?.let {
                    biometricRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.accountRecoveryIntroDialog)

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
            biometricRecovery.logger.logBiometricRecoveryIntroDialogDisplay()
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
