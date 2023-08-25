package com.dashlane.createaccount.pages.choosepassword.validator

import android.view.View
import com.dashlane.passwordstrength.PasswordStrength
import kotlinx.coroutines.Deferred

interface PasswordValidationResultViewProxy {

    fun requiredViewId(): Int

    fun getIncludeLayout(): Int

    fun show(tipsView: View, strengthDeferred: Deferred<PasswordStrength?>)
}