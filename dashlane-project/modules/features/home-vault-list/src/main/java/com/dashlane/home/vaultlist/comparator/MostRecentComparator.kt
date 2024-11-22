package com.dashlane.home.vaultlist.comparator

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.mostRecentAccessTime

fun mostRecentAccessTimeComparator(): Comparator<SummaryObject> = compareByDescending { it.mostRecentAccessTime }