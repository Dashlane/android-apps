package com.dashlane.autofill

import com.dashlane.ext.application.KnownApplication

class AutoFillBlackListImpl(private val additionalPackageNames: Array<String?>? = null) : AutoFillBlackList {

    override fun isBlackList(packageName: String): Boolean {
        return KnownApplication.isAutofillBlackList(packageName) ||
                additionalPackageNames?.contains(packageName) == true
    }
}