package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.OtpLabel
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class OtpFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        super.extractor(view, hintsWithAccuracy, isEditText)
        if (isEditText) {
            addOtpAccuracy(view, hintsWithAccuracy)
        }
    }

    private fun addOtpAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val otpAccuracy = OtpLabel.labelsAccuracy(root)
        if (otpAccuracy > 0) {
            hintsWithAccuracy.add(AutoFillHint.SMS_OTP_CODE, otpAccuracy)
        }
    }
}