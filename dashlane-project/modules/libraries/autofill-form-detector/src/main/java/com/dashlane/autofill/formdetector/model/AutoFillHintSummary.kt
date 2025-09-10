package com.dashlane.autofill.formdetector.model

import android.app.assist.AssistStructure
import android.os.Parcelable
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy
import kotlinx.parcelize.Parcelize

@Parcelize
class AutoFillHintSummary(
    val manualRequest: Boolean,
    val formSource: AutoFillFormSource,
    @AutoFillFormType.FormType
    val formType: Int,
    val focusIndex: Int,
    val entries: List<Entry>,
    val isRequestTimedOut: Boolean,
) : Parcelable {
    val packageName: String
        get() = when (formSource) {
            is ApplicationFormSource -> formSource.packageName
            is WebDomainFormSource -> formSource.packageName
        }

    val webDomain: String?
        get() = when (formSource) {
            is WebDomainFormSource -> formSource.webDomain
            else -> null
        }

    val focusEntry: Entry?
        get() {
            if (focusIndex < 0) {
                return null
            }
            return entries[focusIndex]
        }

    fun isEmpty(): Boolean {
        return entries.isEmpty()
    }

    fun isNotGuessingCredential(): Boolean {
        return this.entries.any { it.hasHint(AutoFillHint.PASSWORD) } &&
            focusEntry?.hasOneOfHints(
                arrayOf(
                    AutoFillHint.PASSWORD,
                    AutoFillHint.USERNAME,
                    AutoFillHint.EMAIL_ADDRESS,
                ),
            ) ?: false
    }

    @Parcelize
    class Entry constructor(
        val id: AutofillId?,
        val autoFillHints: AutofillHintsWithAccuracy,
        var isFocused: Boolean,
        val isHtmlEntry: Boolean,
        val autoFillType: Int,
        val autoFillOptions: Array<String>,
        private val autoFillValue: AutofillValue,
        val viewId: String,
        val parentViewIds: List<String>,
    ) : Parcelable {
        val autoFillValueString: String?
            get() {
                return when {
                    autoFillValue.isText -> autoFillValue.textValue.toString()
                    autoFillValue.isList -> autoFillOptions.getOrNull(autoFillValue.listValue)
                    else -> null
                }
            }

        val autoFillValueTimestamp: Long
            get() {
                return when {
                    autoFillValue.isDate -> autoFillValue.dateValue
                    else -> 0
                }
            }

        fun hasOneOfHints(hints: Array<out String>): Boolean {
            return hints.any { hasAccurateHint(it) }
        }

        fun hasHint(hint: String): Boolean {
            return autoFillHints.hasHint(hint, 0)
        }

        fun hasHintWithAccurate(hint: String, accuracy: Int): Boolean {
            return autoFillHints.hasHint(hint, accuracy)
        }

        fun hasAccurateHint(hint: String): Boolean {
            return autoFillHints.hasHint(hint)
        }
    }
}