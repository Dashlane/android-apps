package com.dashlane.vault.util

import android.content.Context
import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.R
import com.dashlane.vault.model.getLabel
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.utils.Country
import java.util.Locale

fun SummaryObject.Address.getAddressCompleteWithoutName(c: Context): String =
    getAddressCompleteWithoutNameFromResources(c.resources)

fun SummaryObject.Address.getAddressCompleteWithoutNameFromResources(resources: Resources): String {
    val builder = StringBuilder()
    if (addressFull.isNotSemanticallyNull()) {
        builder.append(addressFull).append(", ")
    } else if (streetName.isNotSemanticallyNull()) {
        builder.append(streetName).append(", ")
    }
    if (building.isNotSemanticallyNull()) {
        builder.append(resources.getString(R.string.building)).append(" ").append(building).append(", ")
    }
    if (floor.isNotSemanticallyNull()) {
        builder.append(resources.getString(R.string.floor)).append(" ").append(floor).append(", ")
    }
    if (door.isNotSemanticallyNull()) {
        builder.append(resources.getString(R.string.appartment)).append(" ").append(door).append(", ")
    }
    if (zipCode.isNotSemanticallyNull()) {
        builder.append(zipCode).append(" ")
    }
    if (city.isNotSemanticallyNull()) {
        builder.append(city)
    }
    builder.append(", ").append(getCountry().getLabel(resources))
    return builder.toString().trim().removeSuffix(",")
}

private fun SummaryObject.Address.getCountry(): Country =
    country ?: Country.forIsoCodeOrNull(Locale.getDefault().country.lowercase(Locale.US)) ?: Country.UnitedStates

fun SummaryObject.Address.getFullAddress(context: Context): String =
    getFullAddressFromResources(context.resources)

fun SummaryObject.Address.getFullAddressFromResources(resources: Resources): String {
    val builder = StringBuilder()
    addressName?.let {
        builder.append(addressName).append(" - ")
    }
    builder.append(getAddressCompleteWithoutNameFromResources(resources))
    return builder.toString()
}