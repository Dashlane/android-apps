package com.dashlane.autofill.formdetector.extractor

import android.view.View
import com.dashlane.autofill.api.model.AutoFillApiViewNode
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.AutoFillHintSummaryBuilder
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.formdetector.extractor.form.BrotherViewsFormExtractor
import com.dashlane.autofill.formdetector.extractor.form.CandidateFormExtractor
import com.dashlane.autofill.formdetector.extractor.form.CreditCardFormExtractor
import com.dashlane.autofill.formdetector.extractor.form.FormExtractor
import com.dashlane.autofill.formdetector.extractor.form.LoginFormExtractor
import com.dashlane.autofill.formdetector.extractor.form.OtpFormExtractor
import com.dashlane.autofill.formdetector.extractor.form.PostalAddressFormExtractor
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

class Extractor(
    private val autoFillViewNode: AutoFillViewNode,
    packageName: String,
    needBrotherExtractor: Boolean,
) {
    var website: String? = null
        private set
    private val hintsWithAccuracy = AutofillHintsWithAccuracy()
    private val guessingFormExtractor = mutableListOf(
        LoginFormExtractor(),
        CreditCardFormExtractor(),
        PostalAddressFormExtractor(),
        OtpFormExtractor(),
        CandidateFormExtractor(),
    )

    init {
        if (needBrotherExtractor) {
            guessingFormExtractor.add(BrotherViewsFormExtractor())
        }
        
        val hasFoundHint = HintExtractor(autoFillViewNode).getHint(hintsWithAccuracy)

        if (!hasFoundHint) {
            
            getHintByType()?.let {
                hintsWithAccuracy.add(it, 60)
            }

            
            val isEditText = FormExtractor.isEditText(autoFillViewNode)
            guessingFormExtractor.forEach { formExtractor ->
                formExtractor.extractor(autoFillViewNode, hintsWithAccuracy, isEditText)
            }
        }

        
        if (isUrlBar(packageName, autoFillViewNode)) {
            AutoFillHintSummaryBuilder.getUrlDomain(autoFillViewNode.url)?.let {
                website = it
            }
        }
    }

    fun build(
        autofillValueFactory: AutofillValueFactory,
        viewId: String,
        parentsIds: List<String>,
    ): AutoFillHintSummary.Entry? {
        val autofillType = (autoFillViewNode as? AutoFillApiViewNode)?.viewNode?.autofillType
            ?: View.AUTOFILL_TYPE_TEXT
        val autofillOptions = (autoFillViewNode as? AutoFillApiViewNode)?.viewNode?.autofillOptions
            ?: emptyArray()
        val autofillValue = (autoFillViewNode as? AutoFillApiViewNode)?.viewNode?.autofillValue
            ?: autofillValueFactory.forText("")
        if (hintsWithAccuracy.isEmpty()) {
            return null
        }

        
        if (autoFillViewNode.isFocused) {
            hintsWithAccuracy.addAccuracyToHigher(20)
        }
        return AutoFillHintSummary.Entry(
            id = autoFillViewNode.autofillId,
            autoFillHints = hintsWithAccuracy,
            isFocused = autoFillViewNode.isFocused,
            isHtmlEntry = autoFillViewNode.hasHtmlAttributes,
            autoFillType = autofillType,
            autoFillOptions = toStringArray(autofillOptions),
            autoFillValue = autofillValue,
            viewId = viewId,
            parentViewIds = parentsIds,
        )
    }

    private fun getHintByType(): String? {
        return autoFillViewNode.getHtmlAttribute("type")?.let {
            when (it) {
                "button", "checkbox", "color", "file", "image", "radio", "range", "reset", "search", "submit",
                "time", "url", "week", "date", "datetime-local", "hidden",
                ->
                    
                    null

                "month" -> AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH
                "email" -> AutoFillHint.EMAIL_ADDRESS
                "password" -> AutoFillHint.PASSWORD
                else -> null
            }
        }
    }

    private fun isUrlBar(packageName: String, root: AutoFillViewNode): Boolean {
        val viewIdResourceName = root.viewIdResourceName ?: return false
        return BrowserDetectionHelper.isUrlBar(packageName, viewIdResourceName)
    }

    private fun toStringArray(data: Array<out CharSequence>?): Array<String> {
        return data?.map { it.toString() }?.toTypedArray() ?: arrayOf()
    }
}