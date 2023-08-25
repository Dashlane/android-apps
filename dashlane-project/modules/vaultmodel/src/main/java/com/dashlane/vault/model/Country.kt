package com.dashlane.vault.model

import android.content.Context
import com.dashlane.util.DeviceUtils
import com.dashlane.xml.domain.utils.Country
import java.text.Collator
import java.util.Locale

fun Context.getDefaultCountry(): Country =
    Country.forIsoCodeOrNull(DeviceUtils.getDeviceCountry(this)) ?: Country.UnitedStates

fun Country.Companion.labels(context: Context) =
    values().map { it.getLabel(context) }.sortedWith(Collator.getInstance())

fun Country.Companion.forLabelOrDefault(context: Context, label: String, default: Country = UnitedStates) =
    forLabelOrNull(context, label) ?: default

fun Country.Companion.forLabelOrNull(context: Context, label: String) =
    values().firstOrNull { it.getLabel(context) == label }

fun Country.getLabel(context: Context): String =
    getLabel(buildAppLanguageLocale(context))

@Suppress("IfThenToElvis")
val Country?.signature
    get() = if (this == null) 0 else signature 

private fun buildAppLanguageLocale(context: Context) =
    Locale.Builder()
        .setLanguage(context.getString(R.string.language_iso_639_1))
        .build()

val Country?.isInGreatBritain
    get() = this == Country.UnitedKingdom || this == Country.Ireland