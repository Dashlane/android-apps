package com.dashlane.ui.util

import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.password.generator.PasswordGeneratorWrapper
import com.dashlane.password.generator.generate
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createGeneratedPassword
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObject.GeneratedPassword
import com.dashlane.xml.domain.utils.Country
import java.time.Instant
import javax.inject.Inject

class PasswordGeneratorWrapperImpl @Inject constructor(
    private val passwordGenerator: PasswordGenerator,
    private val preferencesManager: PreferencesManager,
    private val dataSaver: DataSaver,
) : PasswordGeneratorWrapper {
    override suspend fun generatePassword(username: String?, criteria: PasswordGeneratorCriteria?): String {
        val config = criteria ?: getCriteria(username)
        return passwordGenerator.generate(config)
    }

    override fun getCriteria(username: String?): PasswordGeneratorCriteria {
        val userPrefs = preferencesManager[username]
        return PasswordGeneratorCriteria(
            length = userPrefs.getInt(ConstantsPrefs.PASSWORD_GENERATOR_LENGTH, PasswordGenerator.DEFAULT_PASSWORD_LENGTH),
            digits = userPrefs.getBoolean(ConstantsPrefs.PASSWORD_GENERATOR_DIGITS, true),
            symbols = userPrefs.getBoolean(ConstantsPrefs.PASSWORD_GENERATOR_SYMBOLS, true),
            letters = userPrefs.getBoolean(ConstantsPrefs.PASSWORD_GENERATOR_LETTERS, true),
            ambiguousChars = userPrefs.getBoolean(ConstantsPrefs.PASSWORD_GENERATOR_AMBIGUOUS, true),
        )
    }

    override fun saveCriteria(username: String, criteria: PasswordGeneratorCriteria) {
        preferencesManager[username].apply {
            putInt(ConstantsPrefs.PASSWORD_GENERATOR_LENGTH, criteria.length)
            putBoolean(ConstantsPrefs.PASSWORD_GENERATOR_DIGITS, criteria.digits)
            putBoolean(ConstantsPrefs.PASSWORD_GENERATOR_SYMBOLS, criteria.symbols)
            putBoolean(ConstantsPrefs.PASSWORD_GENERATOR_LETTERS, criteria.letters)
            putBoolean(ConstantsPrefs.PASSWORD_GENERATOR_AMBIGUOUS, criteria.ambiguousChars)
        }
    }

    override suspend fun saveToPasswordHistory(
        password: String,
        authDomain: String,
        authId: String,
    ): VaultItem<GeneratedPassword> {
        val gp = createGeneratedPassword(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                uid = generateUniqueIdentifier(),
                id = 0,
                formatLang = Country.UnitedStates,
                syncState = SyncState.MODIFIED,
            ),
            authDomain = authDomain,
            generatedDate = Instant.now().epochSecond.toString(),
            password = password,
            authId = authId,
            platform = SyncObject.Platform.SERVER_ANDROID,
        )
        dataSaver.save(gp)
        return gp
    }
}