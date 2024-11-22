package com.dashlane.secrettransfer

import androidx.annotation.VisibleForTesting
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.lock.LockPass
import com.dashlane.login.LoginMode
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.passphrase.generator.PassphraseGenerator
import com.dashlane.secrettransfer.domain.SecretTransferConstants.SECRET_TRANSFER_SALT
import com.dashlane.secrettransfer.domain.SecretTransferError
import com.dashlane.secrettransfer.domain.SecretTransferException
import com.dashlane.secrettransfer.domain.SecretTransferKeySet
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.domain.SecretTransferUri
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.ui.widgets.compose.Passphrase
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.UserAccountInfo.AccountType
import com.dashlane.user.Username
import com.squareup.moshi.Moshi
import kotlin.random.Random

suspend fun generateKeySet(
    jniCryptography: JniCryptography,
    mplessRequestTransferService: MplessRequestTransferService
): SecretTransferKeySet {
    val response = mplessRequestTransferService.execute()
    val transferId = response.data.transferId
    val (publicKey, privateKey) = jniCryptography.generateX25519KeyPair()
    val secretTransferPublicKey = SecretTransferPublicKey(publicKey)
    val secretTransferUri = SecretTransferUri(transferId = transferId, publicKey = secretTransferPublicKey.raw)
    return SecretTransferKeySet(
        publicKey = secretTransferPublicKey,
        privateKey = privateKey,
        transferId = transferId,
        secretTransferUri = secretTransferUri
    )
}

suspend fun startTransfer(
    jniCryptography: JniCryptography,
    mplessStartTransferService: MplessStartTransferService,
    authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    moshi: Moshi,
    transferId: String,
    publicKey: SecretTransferPublicKey,
    privateKey: String
): SecretTransferPayload {
    val responseData = mplessStartTransferService.execute(
        MplessStartTransferService.Request(
            cryptography = MplessCryptography(
                ellipticCurve = MplessCryptography.EllipticCurve.X25519,
                algorithm = MplessCryptography.Algorithm.DIRECT_HKDF_SHA_256
            ),
            transferId = transferId
        )
    ).data

    return parseResponseData(
        jniCryptography = jniCryptography,
        authenticationSecretTransferRepository = authenticationSecretTransferRepository,
        moshi = moshi,
        responseData = responseData,
        publicKey = publicKey,
        privateKey = privateKey
    )
}

@VisibleForTesting
fun parseResponseData(
    moshi: Moshi,
    jniCryptography: JniCryptography,
    authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    responseData: MplessStartTransferService.Data,
    publicKey: SecretTransferPublicKey,
    privateKey: String
): SecretTransferPayload {
    val symmetricKey: ByteArray = jniCryptography.deriveX25519SharedSecret(
        privateKey = privateKey,
        peerPublicKey = publicKey.toPeerPublicKey(responseData.publicKey),
        salt = SECRET_TRANSFER_SALT.decodeBase64ToByteArray(),
        sharedInfo = byteArrayOf(),
        derivedKeySize = 64
    ) ?: throw Exception()

    return authenticationSecretTransferRepository.decryptStartTransferResponse(symmetricKey, responseData.encryptedData)
        .let { moshi.adapter(SecretTransferPayload::class.java).fromJson(it) } ?: throw Exception()
}

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

suspend fun loginPassword(
    authenticationPasswordRepository: AuthenticationPasswordRepository,
    sessionInitializer: SessionInitializer,
    secretTransferPayload: SecretTransferPayload,
    registeredUserDevice: RegisteredUserDevice.Remote,
    accountType: AccountType
): Triple<SessionResult, Username, LockPass> {
    val username = Username.ofEmail(secretTransferPayload.login)
    val password = AppKey.Password(secretTransferPayload.vaultKey.value, registeredUserDevice.serverKey)
    val result = authenticationPasswordRepository.validateRemoteUser(registeredUserDevice, password)

    val sessionResult = sessionInitializer.createSession(
        username = username,
        appKey = result.password,
        accessKey = result.accessKey,
        secretKey = result.secretKey,
        localKey = result.localKey,
        userSettings = result.settings,
        sharingPublicKey = result.sharingKeys?.public?.value,
        sharingPrivateKey = result.sharingKeys?.private?.value,
        remoteKey = result.remoteKey,
        deviceAnalyticsId = result.deviceAnalyticsId,
        userAnalyticsId = result.userAnalyticsId,
        loginMode = LoginMode.DeviceTransfer,
        accountType = accountType
    )

    return Triple(sessionResult, username, LockPass.ofPassword(password))
}

suspend fun loginSSO(
    secretTransferPayload: SecretTransferPayload,
    registeredUserDevice: RegisteredUserDevice.Remote,
    authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    sessionInitializer: SessionInitializer,
): Triple<SessionResult, Username, LockPass> {
    val username = Username.ofEmail(secretTransferPayload.login)
    val encryptedRemoteKey = registeredUserDevice.encryptedRemoteKey ?: throw AuthenticationInvalidSsoException()

    val ssoKey = AppKey.SsoKey(secretTransferPayload.vaultKey.value.decodeBase64ToByteArray())
    val localKey = authenticationLocalKeyRepository.createForRemote(
        username = username,
        appKey = ssoKey,
        cryptographyMarker = CryptographyMarker.Flexible.Defaults.noDerivation64
    )

    val remoteKey = authenticationSecretTransferRepository.decryptRemoteKey(ssoKey, encryptedRemoteKey)
    val settings = authenticationSecretTransferRepository.decryptSettings(remoteKey, registeredUserDevice.encryptedSettings)
    val sessionResult = sessionInitializer.createSession(
        username = username,
        accessKey = registeredUserDevice.accessKey,
        secretKey = registeredUserDevice.secretKey,
        localKey = localKey,
        userSettings = settings,
        sharingPublicKey = registeredUserDevice.sharingKeys?.publicKey,
        sharingPrivateKey = registeredUserDevice.sharingKeys?.encryptedPrivateKey,
        appKey = ssoKey,
        remoteKey = remoteKey,
        userAnalyticsId = registeredUserDevice.userAnalyticsId,
        deviceAnalyticsId = registeredUserDevice.deviceAnalyticsId,
        loginMode = LoginMode.DeviceTransfer,
        accountType = AccountType.MasterPassword
    )
    return Triple(sessionResult, username, LockPass.ofPassword(ssoKey))
}