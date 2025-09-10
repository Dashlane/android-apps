package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.EmailLabel
import com.dashlane.autofill.formdetector.field.PasswordLabel
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class LoginFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        super.extractor(view, hintsWithAccuracy, isEditText)
        addLoginAccuracy(view, hintsWithAccuracy, isEditText)
        addPasswordAccuracy(view, hintsWithAccuracy, isEditText)
    }

    private fun addLoginAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        if (root.isLogin) {
            hintsWithAccuracy.add(AutoFillHint.USERNAME, 80)
        }
        if (isInputTypeLogin(root)) {
            hintsWithAccuracy.add(AutoFillHint.USERNAME, 20)
        }
        if (isEditText) {
            val otherFieldGuessAccuracy = EmailLabel.labelsAccuracy(root)
            if (otherFieldGuessAccuracy > 0) {
                hintsWithAccuracy.add(AutoFillHint.USERNAME, otherFieldGuessAccuracy)
            }
        }
    }

    private fun addPasswordAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        if (root.isPassword) {
            hintsWithAccuracy.add(AutoFillHint.PASSWORD, 80)
        }
        if (isInputTypePassword(root)) {
            hintsWithAccuracy.add(AutoFillHint.PASSWORD, 35)
        }
        if (isEditText) {
            val otherFieldGuessAccuracy = PasswordLabel.labelsAccuracy(root)
            if (otherFieldGuessAccuracy > 0) {
                hintsWithAccuracy.add(AutoFillHint.PASSWORD, otherFieldGuessAccuracy)
            }
        }
    }
}