package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.CreditCardDateLabel
import com.dashlane.autofill.formdetector.field.CreditCardNameHolderLabel
import com.dashlane.autofill.formdetector.field.CreditCardNumberLabel
import com.dashlane.autofill.formdetector.field.CreditCardSecurityCodeLabel
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class CreditCardFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        super.extractor(view, hintsWithAccuracy, isEditText)
        if (isEditText) {
            addCreditCardNumberAccuracy(view, hintsWithAccuracy)
            addCreditCardDateAccuracy(view, hintsWithAccuracy)
            addCreditCardSecurityCodeAccuracy(view, hintsWithAccuracy)
            addCreditCardNameHolderAccuracy(view, hintsWithAccuracy)
        }
    }

    private fun addCreditCardNumberAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val creditCardNumberAccuracy = CreditCardNumberLabel.labelsAccuracy(root)
        if (creditCardNumberAccuracy > 0) {
            hintsWithAccuracy.add(AutoFillHint.CREDIT_CARD_NUMBER, creditCardNumberAccuracy)
        }
    }

    private fun addCreditCardDateAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val creditCardDateAccuracy = CreditCardDateLabel.labelsAccuracy(root)
        if (creditCardDateAccuracy > 0) {
            hintsWithAccuracy.add(
                AutoFillHint.CREDIT_CARD_EXPIRATION_DATE,
                creditCardDateAccuracy,
            )
        }
    }

    private fun addCreditCardSecurityCodeAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val creditCardSecurityCodeAccuracy = CreditCardSecurityCodeLabel.labelsAccuracy(root)
        if (creditCardSecurityCodeAccuracy > 0) {
            hintsWithAccuracy.add(
                AutoFillHint.CREDIT_CARD_SECURITY_CODE,
                creditCardSecurityCodeAccuracy,
            )
        }
    }

    private fun addCreditCardNameHolderAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val creditCardNameHolderAccuracy = CreditCardNameHolderLabel.labelsAccuracy(root)
        if (creditCardNameHolderAccuracy > 0) {
            hintsWithAccuracy.add(AutoFillHint.NAME, creditCardNameHolderAccuracy)
        }
    }
}