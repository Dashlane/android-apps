package com.dashlane.storage.userdata.accessor.filter.sharing

object NoSharingFilter : SharingFilter {
    override val sharingPermissions: Array<out String>? = null
}