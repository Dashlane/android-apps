package com.dashlane.storage.userdata.accessor.filter.uid

class SpecificUidFilter(override val onlyOnUids: Array<out String>) : UidFilter {
    constructor(onlyOnUid: String) : this(arrayOf(onlyOnUid))
    constructor(onlyOnUids: Collection<String>) : this(onlyOnUids.toTypedArray())
}