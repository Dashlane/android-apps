package com.dashlane.sharing

import androidx.annotation.StringDef

@StringDef(UserPermission.ADMIN, UserPermission.LIMITED, UserPermission.UNDEFINED)
@Retention(AnnotationRetention.SOURCE)
annotation class UserPermission {
    companion object {
        const val ADMIN: String = "admin"
        const val LIMITED: String = "limited"
        const val UNDEFINED: String = "undefined"
    }
}
