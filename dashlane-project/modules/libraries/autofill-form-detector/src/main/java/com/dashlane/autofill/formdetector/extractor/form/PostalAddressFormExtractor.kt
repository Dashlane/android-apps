package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.PostalAddressZipLabel
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class PostalAddressFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        super.extractor(view, hintsWithAccuracy, isEditText)
        if (isEditText) {
            addPostalZipAccuracy(view, hintsWithAccuracy)
        }
    }

    private fun addPostalZipAccuracy(
        root: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
    ) {
        val postalCodeGuessAccuracy = PostalAddressZipLabel.labelsAccuracy(root)
        if (postalCodeGuessAccuracy > 0) {
            hintsWithAccuracy.add(AutoFillHint.POSTAL_CODE, postalCodeGuessAccuracy)
        }
    }
}