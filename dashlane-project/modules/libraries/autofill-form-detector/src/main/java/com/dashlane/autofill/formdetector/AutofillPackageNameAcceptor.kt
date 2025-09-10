package com.dashlane.autofill.formdetector

import com.dashlane.autofill.AutoFillBlackList

class AutofillPackageNameAcceptor(
    private val blacklistPackages: AutoFillBlackList,
) {
    fun canFillRequestForApp(packageName: String?): Boolean {
        return packageName != null && !blacklistPackages.isBlackList(packageName)
    }
}