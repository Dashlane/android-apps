@file:JvmName("AddressUtils")

package com.dashlane.vault.util

import android.content.Context
import com.dashlane.R
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.vault.model.getLabel
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.utils.Country

fun SummaryObject.Address.getAddressCompleteWithoutName(c: Context): String {
    val builder = StringBuilder()
    if (addressFull.isNotSemanticallyNull()) {
        builder.append(addressFull).append(", ")
    } else if (streetName.isNotSemanticallyNull()) {
        builder.append(streetName).append(", ")
    }
    if (building.isNotSemanticallyNull()) {
        builder.append(c.getString(R.string.building)).append(" ").append(building).append(", ")
    }
    if (floor.isNotSemanticallyNull()) {
        builder.append(c.getString(R.string.floor)).append(" ").append(floor).append(", ")
    }
    if (door.isNotSemanticallyNull()) {
        builder.append(c.getString(R.string.appartment)).append(" ").append(door).append(", ")
    }
    if (zipCode.isNotSemanticallyNull()) {
        builder.append(zipCode).append(" ")
    }
    if (city.isNotSemanticallyNull()) {
        builder.append(city)
    }
    builder.append(", ").append(getCountry(c).getLabel(c))
    return builder.toString().trim().removeSuffix(",")
}

fun SummaryObject.Address.getCountry(context: Context): Country =
    country ?: context.getDefaultCountry()

fun SummaryObject.Address.getFullAddress(context: Context): String {
    val builder = StringBuilder()
    addressName?.let {
        builder.append(addressName).append(" - ")
    }
    builder.append(getAddressCompleteWithoutName(context))
    return builder.toString()
}