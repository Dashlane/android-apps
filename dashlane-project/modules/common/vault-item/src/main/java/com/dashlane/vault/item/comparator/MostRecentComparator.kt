package com.dashlane.vault.item.comparator

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.mostRecentAccessTime

fun mostRecentAccessTimeComparator(): Comparator<SummaryObject> = compareByDescending { it.mostRecentAccessTime }