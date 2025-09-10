package com.dashlane.autofill.formdetector.scoring

import android.os.Parcelable
import androidx.annotation.IntRange
import com.dashlane.autofill.formdetector.field.AutoFillHint
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class AutofillHintsWithAccuracy(
    private val data: MutableMap<String, Int> = mutableMapOf(),
) : Parcelable {
    fun add(
        hint: String,
        @IntRange(from = 0, to = 100) toAddPercent: Int,
    ) {
        data[hint] = addPercent(data[hint] ?: 0, toAddPercent)
    }

    fun getHints(
        @IntRange(from = 0, to = 100) minAccuracy: Int = MINIMUM_VALID_ACCURACY,
    ): List<String> {
        return data.toList()
            .filter { (hint, _) -> hint != AutoFillHint.EDIT_TEXT_CANDIDATE && hint != AutoFillHint.EDIT_TEXT_PASSWORD_CANDIDATE }
            .filter { (_, accuracy) -> accuracy >= minAccuracy }
            .sortedByDescending { (_, accuracy) -> accuracy }
            .map { (hint, _) -> hint }
    }

    fun hasHint(
        hint: String,
        @IntRange(from = 0, to = 100) minAccuracy: Int = MINIMUM_VALID_ACCURACY,
    ): Boolean {
        return getHints(minAccuracy).contains(hint)
    }

    fun getHigherPriority(): Int {
        return data.maxBy { (_, accuracy) -> accuracy }.value
    }

    fun getHigherHint(): String {
        return data.maxBy { (_, accuracy) -> accuracy }.key
    }

    fun isEmpty() = data.isEmpty()

    fun addAccuracyToAll(
        @IntRange(from = 0, to = 100) percentToAdd: Int,
    ) {
        return getHints(0).forEach {
            add(it, percentToAdd)
        }
    }

    fun addAccuracyToHigher(
        @IntRange(from = 0, to = 100) percentToAdd: Int,
    ) {
        getHigherHint().let {
            add(it, percentToAdd)
        }
    }

    fun isCandidate(): Boolean = data[AutoFillHint.EDIT_TEXT_CANDIDATE] != null

    fun isPasswordCandidate(): Boolean = data[AutoFillHint.EDIT_TEXT_PASSWORD_CANDIDATE] != null

    companion object {
        private const val MINIMUM_VALID_ACCURACY = 40

        fun addPercent(previousPercent: Int, toAddPercent: Int): Int {
            val missingPercent = 100 - previousPercent
            if (missingPercent <= 0) return previousPercent

            return previousPercent + (missingPercent * toAddPercent / 100.0).roundToInt()
        }
    }
}