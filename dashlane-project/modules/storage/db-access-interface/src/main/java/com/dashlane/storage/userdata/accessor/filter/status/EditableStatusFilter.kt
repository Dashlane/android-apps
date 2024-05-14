package com.dashlane.storage.userdata.accessor.filter.status

interface EditableStatusFilter : StatusFilter {

    var statusFilter: StatusFilter

    fun allStatusFilter() {
        statusFilter = AllStatusFilter
    }
}