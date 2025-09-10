package com.dashlane.vault.item.comparator

import com.dashlane.vault.summary.SummaryObject

fun summaryObjectTypeComparator(): Comparator<SummaryObject> = compareBy { it.syncObjectType }