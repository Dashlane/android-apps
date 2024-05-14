package com.dashlane.storage.userdata.accessor

import android.content.Context
import com.dashlane.storage.userdata.accessor.filter.BaseFilter
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.model.getPhoneNameAndNumber
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

interface GenericDataQuery : DataQuery<SummaryObject, BaseFilter> {
    override fun createFilter(): GenericFilter
}

fun GenericDataQuery.getPhoneList(context: Context, defaultLabel: String): List<Pair<String, String?>> {
    val phoneList = arrayListOf<Pair<String, String?>>()
    val phoneDataIdentifiersList = queryAll(
        genericFilter {
            specificDataType(SyncObjectType.PHONE)
        }
    ).filterIsInstance<SummaryObject.Phone>()
    if (phoneDataIdentifiersList.isNotEmpty()) {
        phoneList.add(Pair(defaultLabel, null))
        phoneDataIdentifiersList.forEach { phone ->
            phone.let {
                phoneList.add(Pair(it.getPhoneNameAndNumber(context), it.id))
            }
        }
    }
    return phoneList
}