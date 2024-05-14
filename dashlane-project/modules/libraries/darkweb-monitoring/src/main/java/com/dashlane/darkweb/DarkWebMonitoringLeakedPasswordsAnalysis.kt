package com.dashlane.darkweb

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.asSharingEncryptedBase64
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.tryOrNull
import com.google.gson.Gson
import javax.inject.Inject

class DarkWebMonitoringLeakedPasswordsAnalysis @Inject constructor(
    private val sharingKeysHelper: SharingKeysHelper,
    private val cryptography: DarkWebMonitoringCryptography
) {
    private val gson = Gson()

    private val userPrivateKey
        get() = sharingKeysHelper.privateKey?.let(SharingKeys::Private)

    fun extractPasswordMap(cipheredInfo: String, cipheredKey: String): Map<String, List<String>>? {
        
        val info = getInfoDeciphered(cipheredInfo = cipheredInfo, cipheredKey = cipheredKey) ?: return null
        
        val infoJson = tryOrNull { gson.fromJson(info, Array<CipheredInfo>::class.java) } ?: return null

        
        val passwordsMap = mutableMapOf<String, MutableList<String>>()
        infoJson.forEach { cipherInfo ->
            val breachId = cipherInfo.breachId ?: return@forEach 
            val passwordListForBreach = passwordsMap[breachId] ?: mutableListOf()
            cipherInfo.data?.forEach { data ->
                if (data.type == "password" && 
                    data.hashMethod == "plaintext" && 
                    !data.value.isSemanticallyNull() 
                ) {
                    passwordListForBreach
                        
                        .add(data.value!!)
                }
            }
            if (passwordListForBreach.isNotEmpty()) {
                passwordsMap[breachId] = passwordListForBreach
            }
        }
        return passwordsMap
    }

    private fun getInfoDeciphered(cipheredInfo: String, cipheredKey: String): String? {
        val privateKey = userPrivateKey ?: return null

        
        val key = cryptography.decryptAlertKey(privateKey, cipheredKey.asSharingEncryptedBase64()) ?: return null
        
        if (key.size != 32) return null
        return cryptography.decryptAlertContent(CryptographyKey.ofBytes32(key), cipheredInfo.asEncryptedBase64())
    }
}