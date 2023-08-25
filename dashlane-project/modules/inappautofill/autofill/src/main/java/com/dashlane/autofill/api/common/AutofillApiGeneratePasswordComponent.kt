package com.dashlane.autofill.api.common

import android.content.Context
import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.util.userfeatures.UserFeaturesChecker

interface AutofillApiGeneratePasswordComponent {
    val generatePasswordService: AutofillGeneratePasswordService
    val generatePasswordLogger: AutofillGeneratePasswordLogger
    val userFeaturesChecker: UserFeaturesChecker

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiGeneratePasswordApplication).component
    }
}
