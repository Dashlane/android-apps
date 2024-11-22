package com.dashlane.home.vaultlist.comparator

import com.dashlane.vault.summary.SummaryObject

fun categoryComparator(): Comparator<SummaryObject> = compareBy { it.syncObjectType }