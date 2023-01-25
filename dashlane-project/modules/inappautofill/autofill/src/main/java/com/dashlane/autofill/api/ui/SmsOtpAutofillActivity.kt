package com.dashlane.autofill.api.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.Bundle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.google.android.gms.auth.api.phone.SmsCodeAutofillClient
import com.google.android.gms.auth.api.phone.SmsCodeRetriever
import com.google.android.gms.common.api.ResolvableApiException



class SmsOtpAutofillActivity : AutoFillResponseActivity() {

    private lateinit var autofillClient: SmsCodeAutofillClient
    private lateinit var bottomSheet: SmsOtpAutofillBottomSheet

    private var broadcastReceivedSmsCode = false

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsCodeRetriever.SMS_CODE_RETRIEVED_ACTION == intent?.action) {
                handleSmsCodeResult(intent.extras)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autofillClient = SmsCodeRetriever.getAutofillClient(this)
        bottomSheet = SmsOtpAutofillBottomSheet(this)
    }

    override fun onStart() {
        super.onStart()
        autofillUsageLog.onClickToAutoFillSmsOtp(getAutofillApiOrigin(forKeyboardAutofill), packageName)
        val intentFilter = IntentFilter(SmsCodeRetriever.SMS_CODE_RETRIEVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)
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
                bottomSheet.showLoader()
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
            bottomSheet.showError(getString(R.string.autofill_sms_otp_dialog_error_title), it.message ?: "?")
        }
    }

    private fun handleSmsCodeResult(extras: Bundle?) {
        broadcastReceivedSmsCode = true
        val smsCode = extras?.getString(SmsCodeRetriever.EXTRA_SMS_CODE)?.takeUnless { it.isBlank() }
        if (smsCode == null) {
            bottomSheet.showError(
                getString(R.string.autofill_sms_otp_dialog_error_title),
                getString(R.string.autofill_sms_otp_dialog_error_detail_timeout)
            )
        } else {
            bottomSheet.showDisplayCode(smsCode)
        }
    }

    companion object {

        private const val REQUEST_CODE_PERMISSION = 4564

        internal fun getIntentSenderForDataset(
            context: Context,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean
        ): IntentSender {
            val intent = createIntent(context, summary, SmsOtpAutofillActivity::class)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            return createIntentSender(context, intent)
        }
    }
}