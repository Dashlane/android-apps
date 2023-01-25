package com.dashlane.storage.userdata.accessor.filter.uid



interface EditableUidFilter : UidFilter {

    var uidFilter: UidFilter

    fun noUidFilter() {
        uidFilter = NoUidFilter
    }

    fun specificUid(vararg uids: String) {
        uidFilter = SpecificUidFilter(uids)
    }

    fun specificUid(uids: Collection<String>) {
        specificUid(*uids.toTypedArray())
    }
}