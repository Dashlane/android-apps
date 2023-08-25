package com.dashlane.createaccount.pages.choosepassword.validator.zxcvbn

import android.content.Context
import android.os.Build
import android.text.Spanned
import android.text.style.BulletSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.text.buildSpannedString
import com.dashlane.R
import com.dashlane.createaccount.pages.choosepassword.validator.PasswordValidationResultViewProxy
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthHorizontalIndicatorView
import com.dashlane.passwordstrength.PasswordStrengthSuggestion
import com.dashlane.passwordstrength.PasswordStrengthWarning
import com.dashlane.passwordstrength.getYourPasswordIsLabel
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PasswordValidationResultByZxcvbnViewProxy(private val scope: CoroutineScope) : PasswordValidationResultViewProxy {

    private var previousJob: Job? = null

    override fun requiredViewId() = R.id.suggestions
    override fun getIncludeLayout() = R.layout.include_password_zxcvbn_tips

    override fun show(tipsView: View, strengthDeferred: Deferred<PasswordStrength?>) {
        previousJob?.cancel()
        previousJob = scope.launch(Dispatchers.Main) {
            
            val passwordStrength = strengthDeferred.await()
            val context = tipsView.context

            val passwordStrengthTextView = tipsView.findViewById<TextView>(R.id.password_strength)
            val strengthHorizontalIndicatorView =
                tipsView.findViewById<PasswordStrengthHorizontalIndicatorView>(R.id.password_strength_indicator)

            if (passwordStrength == null) {
                
                strengthHorizontalIndicatorView.visibility = View.GONE
                passwordStrengthTextView.setText(R.string.password_creation_empty_title)
                setTextOrGone(
                    tipsView,
                    R.id.suggestions,
                    getFormattedTips(
                        tipsView,
                        listOf(
                            context.getString(R.string.password_creation_empty_suggestion_1),
                            context.getString(R.string.password_creation_empty_suggestion_2)
                        )
                    )
                )
            } else {
                
                strengthHorizontalIndicatorView.visibility = View.VISIBLE
                strengthHorizontalIndicatorView.setPasswordStrength(passwordStrength)

                passwordStrengthTextView.text = passwordStrength.getYourPasswordIsLabel(context)

                val allTips = passwordStrength.getAllTips(context)
                setTextOrGone(tipsView, R.id.suggestions, getFormattedTips(tipsView, allTips))
            }
        }
    }

    private fun getFormattedTips(tipsView: View, allTips: List<String>) =
        buildSpannedString {
            allTips.forEach {
                if (length > 0) append("\n\n")
                val sizeBefore = length
                append(it)
                setSpan(createBulletSpan(tipsView), sizeBefore, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

    private fun createBulletSpan(tipsView: View): BulletSpan {
        val marginBullet = tipsView.resources.dpToPx(8F).roundToInt()
        val colorBullet = tipsView.context.getThemeAttrColor(R.attr.colorOnSurface)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BulletSpan(marginBullet, colorBullet, tipsView.resources.dpToPx(2F).roundToInt())
        } else {
            BulletSpan(marginBullet, colorBullet)
        }
    }

    private fun setTextOrGone(tipsView: View, @IdRes viewId: Int, value: CharSequence) {
        val view = tipsView.findViewById<TextView>(viewId)
        if (value.isBlank()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = value
        }
    }
}

private fun PasswordStrength.getAllTips(context: Context): List<String> {
    val warning = warning?.let { context.getString(it.getTextResId()) }
    return if (warning == null) {
        suggestions.map { context.getString(it.getTextResId()) }
    } else {
        val suggestion = suggestions.firstOrNull()?.let { context.getString(it.getTextResId()) }
        if (suggestion == null) {
            listOf(warning)
        } else {
            listOf(warning, suggestion)
        }
    }
}

@StringRes
private fun PasswordStrengthWarning.getTextResId() = when (this) {
    PasswordStrengthWarning.ROWS_OF_KEYS -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_straight_rows_of_keys_are_easy_to_guess
    PasswordStrengthWarning.SHORT_KEYBOARD_PATTERNS -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_short_keyboard_patterns_are_easy_to_guess
    PasswordStrengthWarning.REPEATS_SEQUENCE -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_repeats_like_abcabcabc_are_only_slightly_harder_to_guess_than_abc
    PasswordStrengthWarning.REPEATS_CHARACTER -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_repeats_like_aaa_are_easy_to_guess
    PasswordStrengthWarning.SEQUENCES -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_sequences_like_abc_or_6543_are_easy_to_guess
    PasswordStrengthWarning.RECENT_YEARS -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_recent_years_are_easy_to_guess
    PasswordStrengthWarning.DATES -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_dates_are_often_easy_to_guess
    PasswordStrengthWarning.TOP_10_COMMON -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_this_is_a_top_10_common_password
    PasswordStrengthWarning.TOP_100_COMMON -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_this_is_a_top_100_common_password
    PasswordStrengthWarning.COMMON -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_this_is_a_very_common_password
    PasswordStrengthWarning.SIMILAR_TO_COMMON -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_this_is_similar_to_a_commonly_used_password
    PasswordStrengthWarning.WORD -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_a_word_by_itself_is_easy_to_guess
    PasswordStrengthWarning.NAMES -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_names_and_surnames_by_themselves_are_easy_to_guess
    PasswordStrengthWarning.COMMON_NAMES -> com.dashlane.passwordstrength.R.string.zxcvbn_warning_common_names_and_surnames_are_easy_to_guess
}

@StringRes
private fun PasswordStrengthSuggestion.getTextResId() = when (this) {
    PasswordStrengthSuggestion.USE_WORDS -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_use_a_few_words_avoid_common_phrases
    PasswordStrengthSuggestion.USELESS_SYMBOLS_DIGITS_UPPERCASE -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_no_need_for_symbols_digits_or_uppercase_letters
    PasswordStrengthSuggestion.ADD_MORE_WORDS -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_add_another_word_or_two_uncommon_words_are_better
    PasswordStrengthSuggestion.USE_LONGER_KEYBOARD_PATTERN -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_use_a_longer_keyboard_pattern_with_more_turns
    PasswordStrengthSuggestion.AVOID_REPEATS -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_avoid_repeated_words_and_characters
    PasswordStrengthSuggestion.AVOID_SEQUENCES -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_avoid_sequences
    PasswordStrengthSuggestion.AVOID_RECENT_YEARS -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_avoid_recent_years
    PasswordStrengthSuggestion.AVOID_PERSONAL_YEARS -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_avoid_years_that_are_associated_with_you
    PasswordStrengthSuggestion.AVOID_PERSONAL_DATES -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_avoid_dates_and_years_that_are_associated_with_you
    PasswordStrengthSuggestion.USELESS_CAPITALIZATION -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_capitalization_doesnt_help_very_much
    PasswordStrengthSuggestion.USELESS_UPPERCASE -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_all_uppercase_is_almost_as_easy_to_guess_as_all_lowercase
    PasswordStrengthSuggestion.USELESS_REVERSED -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_reversed_words_arent_much_harder_to_guess
    PasswordStrengthSuggestion.USELESS_SUBSTITUTION -> com.dashlane.passwordstrength.R.string.zxcvbn_suggestion_predictable_substitutions_like_at_instead_of_a_dont_help_very_much
}