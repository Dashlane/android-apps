package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.model.getPhoneNameAndNumber
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType



@Suppress("UNCHECKED_CAST")
fun MainDataAccessor.getPhoneList(context: Context, defaultLabel: String): List<Pair<String, String?>> {
    val phoneList = arrayListOf<Pair<String, String?>>()
    val phoneDataIdentifiersList = getGenericDataQuery().queryAll(
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