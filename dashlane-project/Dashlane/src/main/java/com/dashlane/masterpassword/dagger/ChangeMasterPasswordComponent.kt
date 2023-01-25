package com.dashlane.masterpassword.dagger

import com.dashlane.masterpassword.ChangeMasterPasswordActivity
import com.dashlane.masterpassword.ChangeMasterPasswordLogoutHelper
import com.dashlane.masterpassword.MasterPasswordChanger

interface ChangeMasterPasswordComponent {
    val changeMasterPasswordLogoutHelper: ChangeMasterPasswordLogoutHelper
    val masterPasswordChanger: MasterPasswordChanger
    fun inject(activity: ChangeMasterPasswordActivity)
}