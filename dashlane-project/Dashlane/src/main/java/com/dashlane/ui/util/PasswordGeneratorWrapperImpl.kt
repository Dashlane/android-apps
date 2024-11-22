package com.dashlane.ui.util

import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordgenerator.criteria
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.booleanPreference
import com.dashlane.preference.intPreference
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.ui.util.PasswordGeneratorCreator.provideGeneratedPassword
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject.GeneratedPassword
import javax.inject.Inject

class PasswordGeneratorWrapperImpl @Inject constructor(
    private val strength: PasswordGeneratorAndStrength,
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    private val dataSaver: DataSaver
) : PasswordGeneratorWrapper {

    override suspend fun generatePassword(criteria: PasswordGeneratorCriteria?): PasswordGeneratorWrapper.Result {
        val config = criteria ?: this.criteria
        return strength.generate(config)
    }

    override var passwordLength: Int
            by preferencesManager[sessionManager.session?.username].intPreference(
                ConstantsPrefs.PASSWORD_GENERATOR_LENGTH,
                PasswordGenerator.DEFAULT_PASSWORD_LENGTH
            )

    override var isUsingAmbiguousChar: Boolean
            by preferencesManager[sessionManager.session?.username].booleanPreference(ConstantsPrefs.PASSWORD_GENERATOR_AMBIGUOUS, false)

    override var isUsingSymbols: Boolean
            by preferencesManager[sessionManager.session?.username].booleanPreference(ConstantsPrefs.PASSWORD_GENERATOR_SYMBOLS, true)

    override var isUsingLetters: Boolean
            by preferencesManager[sessionManager.session?.username].booleanPreference(ConstantsPrefs.PASSWORD_GENERATOR_LETTERS, true)

    override var isUsingDigits: Boolean
            by preferencesManager[sessionManager.session?.username].booleanPreference(ConstantsPrefs.PASSWORD_GENERATOR_DIGITS, true)

    override suspend fun saveToPasswordHistory(
        password: String,
        authDomain: String,
        authId: String
    ): VaultItem<GeneratedPassword> {
        val gp = provideGeneratedPassword(
            authDomain,
            password,
            authId
        )
        dataSaver.save(gp)
        return gp
    }
}