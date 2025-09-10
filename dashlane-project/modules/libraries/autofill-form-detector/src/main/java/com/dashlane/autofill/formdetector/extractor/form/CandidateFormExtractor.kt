package com.dashlane.autofill.formdetector.extractor.form

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class CandidateFormExtractor : FormExtractor {
    override fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        
        if (isEditText && isInputTypePassword(view)) {
            hintsWithAccuracy.add(AutoFillHint.EDIT_TEXT_PASSWORD_CANDIDATE, 10)
        } else if (isEditText && hintsWithAccuracy.isEmpty()) {
            hintsWithAccuracy.add(AutoFillHint.EDIT_TEXT_CANDIDATE, 10)
        }
    }
}