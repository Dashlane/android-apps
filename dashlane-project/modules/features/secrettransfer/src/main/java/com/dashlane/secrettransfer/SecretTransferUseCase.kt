package com.dashlane.secrettransfer

import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountInfo.AccountType
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.passphrase.generator.PassphraseGenerator
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.session.AppKey
import com.dashlane.ui.widgets.compose.Passphrase
import kotlin.random.Random

fun getPayload(
    email: String,
    appKey: AppKey,
    userAccountInfo: UserAccountInfo,
    token: String?,
): SecretTransferPayload {
    val vaultKey = when (appKey) {
        is AppKey.Password -> appKey.passwordUtf8Bytes.decodeUtf8ToString()
        is AppKey.SsoKey -> appKey.cryptographyKeyBytes.toByteArray().encodeBase64ToString()
    }

    val type = when {
        userAccountInfo.sso -> SecretTransferPayload.Type.SSO
        userAccountInfo.accountType is AccountType.InvisibleMasterPassword -> SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD
        userAccountInfo.accountType is AccountType.MasterPassword -> SecretTransferPayload.Type.MASTER_PASSWORD
        else -> throw SecretTransferException(SecretTransferError.InvalidSession)
    }

    return SecretTransferPayload(
        login = email,
        vaultKey = SecretTransferPayload.VaultKey(type, vaultKey),
        token = token
    )
}

fun generatePassphrase(
    passphraseGenerator: PassphraseGenerator,
    sodiumCryptography: SodiumCryptography,
    sessionKey: ByteArray,
    login: String,
    transferId: String,
): List<Passphrase> {
    val wordList = passphraseGenerator.getWordList()
    val passphraseMessage = "${SodiumCryptography.WORDSEED_KEY_HEADER}${login.length}$login$transferId".encodeUtf8ToByteArray()
    val passphraseSeed = sodiumCryptography.genericHash(passphraseMessage, sessionKey)
    return passphraseGenerator.generatePassphrase(wordList, passphraseSeed).map { word -> Passphrase.Word(value = word) }
}

fun generatePassphraseWithMissingWord(
    passphraseGenerator: PassphraseGenerator,
    sodiumCryptography: SodiumCryptography,
    sessionKey: ByteArray,
    login: String,
    transferId: String
): List<Passphrase> {
    val passphrase = generatePassphrase(passphraseGenerator, sodiumCryptography, sessionKey, login, transferId)
    val randomIndex = Random.nextInt(0, passphrase.lastIndex)
    return passphrase.mapIndexed { index, word ->
        when (index) {
            randomIndex -> Passphrase.Missing(value = word.value, userInput = "", isError = false)
            else -> word
        }
    }
}

suspend fun generateExtraDeviceToken(
    extraDeviceTokenGeneratorService: AuthRegistrationExtraDeviceTokenGeneratorService,
    authorization: Authorization.User
) = extraDeviceTokenGeneratorService.execute(
    userAuthorization = authorization,
    request = AuthRegistrationExtraDeviceTokenGeneratorService.Request(
        AuthRegistrationExtraDeviceTokenGeneratorService.Request.TokenType.SHORTLIVED
    )
).data

class SecretTransferException(val error: SecretTransferError) : Exception()

sealed class SecretTransferError {
    data object InvalidSession : SecretTransferError()
    data object CryptographicError : SecretTransferError()
    data object ServerError : SecretTransferError()
    data object Generic : SecretTransferError()
}
