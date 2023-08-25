package com.dashlane.autofill

import com.dashlane.ext.application.BlacklistApplication

class AutoFillBlackListImpl(private val additionalPackageNames: Array<String?>? = null) : AutoFillBlackList {

    override fun isBlackList(packageName: String): Boolean {
        return BlacklistApplication.isAutofillBlackList(packageName) ||
                additionalPackageNames?.contains(packageName) == true
    }
}