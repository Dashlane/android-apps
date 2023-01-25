package com.dashlane.accountrecovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.lock.LockHelper.Companion.PROMPT_LOCK_FOR_SETTINGS
import com.dashlane.lock.UnlockEvent
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.showToaster
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountRecoveryIntroDialogActivity : DashlaneActivity() {

    @Inject
    @GlobalCoroutineScope
    lateinit var globalCoroutineScope: CoroutineScope

    private val accountRecovery
        get() = SingletonProvider.getComponent().accountRecovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountRecovery.isFeatureKnown = true

        showBottomSheet()
    }

    private fun showBottomSheet() = BottomSheetDialog(this).apply {
        setContentView(R.layout.dialog_account_recovery_intro)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        findViewById<View>(R.id.positive_cta)?.setOnClickListener {
            globalCoroutineScope.launch(Dispatchers.Main) {
                lockHelper.showAndWaitLockActivityForReason(
                    this@AccountRecoveryIntroDialogActivity,
                    UnlockEvent.Reason.WithCode(UNLOCK_EVENT_CODE),
                    PROMPT_LOCK_FOR_SETTINGS,
                    getString(R.string.please_enter_master_password_to_edit_settings)
                )?.takeIf { unlockEvent ->
                    val reason = unlockEvent.reason
                    unlockEvent.isSuccess() &&
                            reason is UnlockEvent.Reason.WithCode &&
                            reason.requestCode == UNLOCK_EVENT_CODE
                }?.let {
                    accountRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.accountRecoveryIntroDialog)

                    this@AccountRecoveryIntroDialogActivity.showToaster(
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
            accountRecovery.logger.logAccountRecoveryIntroDialogDisplay()
        }

        setOnCancelListener {
            finish()
        }
    }.show()

    companion object {
        private const val UNLOCK_EVENT_CODE = 8747

        fun newIntent(context: Context) = Intent(context, AccountRecoveryIntroDialogActivity::class.java)
    }
}