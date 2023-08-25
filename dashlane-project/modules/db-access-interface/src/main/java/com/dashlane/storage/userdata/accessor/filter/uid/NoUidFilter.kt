package com.dashlane.storage.userdata.accessor.filter.uid

object NoUidFilter : UidFilter {
    override val onlyOnUids: Array<out String>? = null
}