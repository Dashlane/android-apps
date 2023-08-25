package com.dashlane.autofill.api.ui

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.model.OtpItemToFill
import com.dashlane.hermes.generated.definitions.MatchType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.min
import kotlin.math.roundToInt

class SmsOtpAutofillBottomSheet(private val activity: SmsOtpAutofillActivity) {

    private val dialog: BottomSheetDialog = BottomSheetDialog(activity)
    private val titleTextView: TextView
    private val messageTextView: TextView
    private val googleImageView: ImageView
    private val progressBar: ProgressBar
    private val codeTextView: TextView
    private val cancelButton: Button
    private val autofillButton: Button

    init {
        val view = View.inflate(activity, R.layout.layout_bottom_sheet_sms, null)
        dialog.setContentView(view)
        setupHeightDialog(view)
        dialog.setOnCancelListener { activity.finish() }

        titleTextView = view.findViewById(R.id.title_textview)
        messageTextView = view.findViewById(R.id.message_textview)
        codeTextView = view.findViewById(R.id.code_textview)
        cancelButton = view.findViewById(R.id.cancel_button)
        autofillButton = view.findViewById(R.id.autofill_button)
        googleImageView = view.findViewById(R.id.google_imageview)
        progressBar = view.findViewById(R.id.progress_bar)

        cancelButton.setOnClickListener { dialog.cancel() }
        autofillButton.setOnClickListener {
            activity.finishWithResult(
                itemToFill = OtpItemToFill(code = codeTextView.text.toString()),
                autofillFeature = AutofillFeature.SMS_OTP_CODE,
                matchType = MatchType.REGULAR
            )
        }
    }

    fun showLoader() {
        titleTextView.text = titleTextView.context.getString(R.string.autofill_sms_otp_dialog_waiting_title)
        messageTextView.visibility = View.GONE
        codeTextView.visibility = View.GONE
        autofillButton.visibility = View.GONE
        googleImageView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        showDialogIfNotAlready()
    }

    fun showDisplayCode(code: String) {
        titleTextView.text = titleTextView.context.getString(R.string.autofill_sms_otp_dialog_success_title)
        messageTextView.visibility = View.GONE
        codeTextView.visibility = View.VISIBLE
        autofillButton.visibility = View.VISIBLE
        googleImageView.visibility = View.GONE
        progressBar.visibility = View.GONE
        codeTextView.text = code

        codeTextView.setOnClickListener {
            activity.finishWithResult(
                itemToFill = OtpItemToFill(code = code),
                autofillFeature = AutofillFeature.SMS_OTP_CODE,
                matchType = MatchType.REGULAR
            )
        }
        showDialogIfNotAlready()
    }

    fun showError(title: String, message: String) {
        titleTextView.text = title
        messageTextView.text = message
        messageTextView.visibility = View.VISIBLE
        codeTextView.visibility = View.GONE
        autofillButton.visibility = View.GONE
        googleImageView.visibility = View.GONE
        progressBar.visibility = View.GONE
        showDialogIfNotAlready()
    }

    private fun showDialogIfNotAlready() {
        if (!dialog.isShowing) dialog.show()
    }

    private fun setupHeightDialog(view: View) {
        val displayMetrics = activity.resources.displayMetrics
        
        val desiredHeight = min((displayMetrics.density * 210).roundToInt(), displayMetrics.heightPixels)
        val layoutParams = view.layoutParams
        layoutParams.height = desiredHeight
        view.layoutParams = layoutParams
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = desiredHeight
    }
}