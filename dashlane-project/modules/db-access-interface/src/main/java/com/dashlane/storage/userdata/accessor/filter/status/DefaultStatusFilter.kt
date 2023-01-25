package com.dashlane.storage.userdata.accessor.filter.status



object DefaultStatusFilter : StatusFilter {
    override val onlyVisibleStatus: Boolean = true
}