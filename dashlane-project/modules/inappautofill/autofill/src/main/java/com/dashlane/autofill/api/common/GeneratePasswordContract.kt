package com.dashlane.autofill.api.common

import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.definition.Base



class GeneratePasswordContract {

    interface Presenter : Base.IPresenter {

        

        fun generatePassword(criteria: PasswordGeneratorCriteria)

        

        fun onGenerateButtonClicked(criteria: PasswordGeneratorCriteria)

        

        fun onGeneratorConfigurationChanged(criteria: PasswordGeneratorCriteria)

        

        fun onPasswordUpdated(password: String)

        

        suspend fun saveGeneratedPasswordIfUsed(result: VaultItem<SyncObject.Authentifiant>)

        

        fun initSpecialMode()
    }

    interface ViewProxy : Base.IView {

        

        fun setPasswordField(newPassword: String?)

        

        fun setPasswordStrength(title: String?, color: Int, strength: Int, safeEnoughForSpecialMode: Boolean)

        

        fun initSpecialMode(eligible: Boolean)
    }

    interface DataProvider : Base.IDataProvider {
        

        suspend fun generatePassword(criteria: PasswordGeneratorCriteria): PasswordGeneratorWrapper.Result

        

        suspend fun evaluatePassword(password: String): PasswordStrengthScore?

        

        fun getPasswordGeneratorDefaultConfiguration(): PasswordGeneratorCriteria

        

        fun setPasswordGeneratorDefaultConfiguration(criteria: PasswordGeneratorCriteria)

        

        suspend fun saveToPasswordHistory(password: String, itemDomain: String, itemUid: String)
    }
}