package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.EmailLabel
import com.dashlane.autofill.formdetector.field.PasswordLabel
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.model.children
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class BrotherViewsFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        super.extractor(view, hintsWithAccuracy, isEditText)
        if (isEditText) {
            view.superParent?.children?.forEach { child ->
                if (!FormExtractor.isTextView(child)) return@forEach
                val childFieldGuessAccuracyPassword = PasswordLabel.labelsAccuracy(child)
                val childFieldGuessAccuracy = EmailLabel.labelsAccuracy(child)
                if (childFieldGuessAccuracyPassword > 0) {
                    hintsWithAccuracy.add(
                        AutoFillHint.EDIT_TEXT_PASSWORD_CANDIDATE,
                        childFieldGuessAccuracyPassword,
                    )
                }
                if (childFieldGuessAccuracy > 0) {
                    hintsWithAccuracy.add(AutoFillHint.EDIT_TEXT_CANDIDATE, childFieldGuessAccuracy)
                }
            }
        }
    }
}