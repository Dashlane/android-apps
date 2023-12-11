package com.dashlane.autofill.changepause.services

import android.content.Context
import com.dashlane.autofill.api.R
import com.dashlane.autofill.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChangePauseStringsFromContext @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val autofillFormSourcesStrings: AutofillFormSourcesStrings
) : ChangePauseStrings {

    override fun getAutofillFromSourceTitle(autoFillFormSource: AutoFillFormSource): String {
        return autoFillFormSource.title
    }

    override fun getPauseTitle(autoFillFormSource: AutoFillFormSource): String {
        return "${autoFillFormSource.title} - ${autoFillFormSource.type}"
    }

    private val AutoFillFormSource.title: String
        get() = autofillFormSourcesStrings.getAutoFillFormSourceString(this)

    private val AutoFillFormSource.type: String
        get() = autofillFormSourcesStrings.getAutoFillFormSourceTypeString(this)

    override fun getPausePermanentMessage(autoFillFormSource: AutoFillFormSource): String {
        return autoFillFormSource.getPausePermanentString()
    }

    override fun getPauseForHoursMessage(autoFillFormSource: AutoFillFormSource, hours: Int): String {
        return autoFillFormSource.getPauseForHoursString(hours)
    }

    override fun getPauseForMinutesMessage(autoFillFormSource: AutoFillFormSource, minutes: Int): String {
        return autoFillFormSource.getPauseForMinutesString(minutes)
    }

    override fun getNotPausedMessage(autoFillFormSource: AutoFillFormSource): String {
        return autoFillFormSource.getNotPausedString()
    }

    private fun AutoFillFormSource.getPausePermanentString(): String {
        return when (this) {
            is ApplicationFormSource -> context.getString(R.string.autofill_changepause_application_permanent_paused)
            is WebDomainFormSource -> context.getString(R.string.autofill_changepause_website_permanent_paused)
        }
    }

    private fun AutoFillFormSource.getPauseForHoursString(hours: Int): String {
        return when (this) {
            is ApplicationFormSource ->
                context.resources.getQuantityString(
                    R.plurals.autofill_changepause_application_paused_by_hours,
                    hours,
                    hours
                )
            is WebDomainFormSource ->
                context.resources.getQuantityString(
                    R.plurals.autofill_changepause_website_paused_by_hours,
                    hours,
                    hours
                )
        }
    }

    private fun AutoFillFormSource.getPauseForMinutesString(minutes: Int): String {
        return when (this) {
            is ApplicationFormSource ->
                context.resources.getQuantityString(
                    R.plurals.autofill_changepause_application_paused_by_minutes,
                    minutes,
                    minutes
                )
            is WebDomainFormSource ->
                context.resources.getQuantityString(
                    R.plurals.autofill_changepause_website_paused_by_minutes,
                    minutes,
                    minutes
                )
        }
    }

    private fun AutoFillFormSource.getNotPausedString(): String {
        return when (this) {
            is ApplicationFormSource -> context.getString(R.string.autofill_changepause_application_not_paused)
            is WebDomainFormSource -> context.getString(R.string.autofill_changepause_website_not_paused)
        }
    }
}
