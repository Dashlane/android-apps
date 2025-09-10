package com.dashlane.autofill.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.smsotp.SmsOtpAutofillViewModel
import com.dashlane.autofill.smsotp.SmsOtpBottomSheetScreen
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.registerExportedReceiverCompat
import com.google.android.gms.auth.api.phone.SmsCodeAutofillClient
import com.google.android.gms.auth.api.phone.SmsCodeRetriever
import com.google.android.gms.common.api.ResolvableApiException

class SmsOtpAutofillActivity : AutoFillResponseActivity() {
    private lateinit var autofillClient: SmsCodeAutofillClient
    private val viewModel by viewModels<SmsOtpAutofillViewModel>()

    private var broadcastReceivedSmsCode = false

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsCodeRetriever.SMS_CODE_RETRIEVED_ACTION == intent?.action) {
                viewModel.onSmsCodeResult(intent.extras)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        autofillClient = SmsCodeRetriever.getAutofillClient(this)
        setContent {
            DashlaneTheme {
                SmsOtpBottomSheetScreen(
                    viewModel = viewModel,
                    onSuccess = ::onSuccess,
                    onDismiss = ::onDismiss,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(SmsCodeRetriever.SMS_CODE_RETRIEVED_ACTION)
        registerExportedReceiverCompat(smsReceiver, intentFilter)
        fetchSmsCode()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsReceiver)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSION && resultCode == Activity.RESULT_OK) {
            fetchSmsCode()
            return
        }
        finish()
    }

    private fun fetchSmsCode() {
        val task = autofillClient.startSmsCodeRetriever()
        task.addOnSuccessListener {
            if (!broadcastReceivedSmsCode) {
                viewModel.onFetchSmsCode()
            }
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(this, REQUEST_CODE_PERMISSION)
                    return@addOnFailureListener
                } catch (e: Exception) {
                    
                }
            }
            viewModel.onFetchSmsCodeFailure(exception = it)
        }
    }

    private fun onSuccess(smsCode: String) {
        finishWithResult(
            itemToFill = OtpItemToFill(code = smsCode),
            matchType = MatchType.REGULAR,
        )
    }

    private fun onDismiss() {
        finish()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 4564

        internal fun getIntentSenderForDataset(
            context: Context,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean,
            isAccountFrozen: Boolean,
        ): IntentSender {
            if (isAccountFrozen) {
                return FrozenAutofillActivity.getPendingIntent(context, summary)
            }
            val intent = createIntent(context, summary, SmsOtpAutofillActivity::class)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            return createIntentSender(context, intent)
        }
    }
}