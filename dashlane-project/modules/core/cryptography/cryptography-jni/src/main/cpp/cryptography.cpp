#include <cstring>
#include <jni.h>
#include <string>
#include <cinttypes>

#include <openssl/evp.h>
#include <openssl/asn1.h>
#include <openssl/des.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/bio.h>
#include <openssl/rand.h>
#include <openssl/engine.h>
#include <openssl/conf.h>
#include <openssl/hmac.h>
#include <openssl/evp.h>
#include <openssl/kdf.h>

#include "argon2.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "JNIConvertor.h"
#include "SharingV2Crypto.h"
#include "Utils.h"
#include "RSACipherUtils.h"

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_deriveKeyArgon2d(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jbyteArray key,
                                                                           jbyteArray salt,
                                                                           jint t_cost,
                                                                           jint m_cost,
                                                                           jint parallelism,
                                                                           jint derivedKeySize) {
    // Convert JNI Data type to native type
    char *native_key = JNIConvertor::as_char_array(env, key);
    char *native_salt = JNIConvertor::as_char_array(env, salt);

    int native_keyLength = env->GetArrayLength(key);
    int native_saltLength = env->GetArrayLength(salt);

    //
    uint8_t derivedKeyResult[derivedKeySize];

    // high-level API
    argon2d_hash_raw(t_cost,
                     m_cost,
                     parallelism,
                     native_key,
                     native_keyLength,
                     native_salt,
                     native_saltLength,
                     derivedKeyResult,
                     derivedKeySize);

    return JNIConvertor::as_byte_array(env, derivedKeyResult, derivedKeySize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_deriveKeyArgon2i(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jbyteArray key,
                                                                           jbyteArray salt,
                                                                           jint t_cost,
                                                                           jint m_cost,
                                                                           jint parallelism,
                                                                           jint derivedKeySize) {
    // Convert JNI Data type to native type
    char *native_key = JNIConvertor::as_char_array(env, key);
    char *native_salt = JNIConvertor::as_char_array(env, salt);

    int native_keyLength = env->GetArrayLength(key);
    int native_saltLength = env->GetArrayLength(salt);

    //
    uint8_t derivedKeyResult[derivedKeySize];

    // high-level API
    argon2i_hash_raw(t_cost,
                     m_cost,
                     parallelism,
                     native_key,
                     native_keyLength,
                     native_salt,
                     native_saltLength,
                     derivedKeyResult,
                     derivedKeySize);

    return JNIConvertor::as_byte_array(env, derivedKeyResult, derivedKeySize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_deriveKeyPBKDF2sha256(
        JNIEnv *env, jobject thiz,
        jbyteArray key,
        jbyteArray salt,
        jint iterationCount,
        jint derivedKeySize) {
    // Convert JNI Data type to native type
    char *native_key = JNIConvertor::as_char_array(env, key);
    unsigned char *native_salt = JNIConvertor::as_unsigned_char_array(env, salt);

    int native_keyLength = env->GetArrayLength(key);
    int native_saltLength = env->GetArrayLength(salt);

    uint8_t derivedKeyResult[derivedKeySize];


    PKCS5_PBKDF2_HMAC(native_key, native_keyLength, native_salt, native_saltLength, iterationCount,
                      EVP_sha256(),
                      derivedKeySize, derivedKeyResult);

    return JNIConvertor::as_byte_array(env, derivedKeyResult, derivedKeySize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_deriveKeyPBKDF2sha1(
        JNIEnv *env, jobject thiz,
        jbyteArray key,
        jbyteArray salt,
        jint iterationCount,
        jint derivedKeySize) {
    // Convert JNI Data type to native type
    char *native_key = JNIConvertor::as_char_array(env, key);
    unsigned char *native_salt = JNIConvertor::as_unsigned_char_array(env, salt);

    int native_keyLength = env->GetArrayLength(key);
    int native_saltLength = env->GetArrayLength(salt);

    uint8_t derivedKeyResult[derivedKeySize];


    PKCS5_PBKDF2_HMAC(native_key, native_keyLength, native_salt, native_saltLength, iterationCount,
                      EVP_sha1(),
                      derivedKeySize, derivedKeyResult);

    return JNIConvertor::as_byte_array(env, derivedKeyResult, derivedKeySize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_mutateKeyKWC3(JNIEnv *env, jobject thiz,
                                                                        jbyteArray key) {
    return JNIConvertor::fromUtf8ToUtf16(env, key);

}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_encryptContentRSA(JNIEnv *env,
                                                                            jobject obj,
                                                                            jbyteArray jdata,
                                                                            jstring jpass) {
    string pass = JNIConvertor::fromJStringToString(env, jpass);
    string data = JNIConvertor::fromJByteArrayToString(env, jdata);

    unsigned char *encryptedContent;
    int resultSize = -1;

    try {
        encryptedContent = SharingV2Crypto::encryptContentRSA(data, pass, resultSize);
    }
    catch (std::exception *e) {}
    catch (std::exception &e) {}
    catch (...) {}

    if (resultSize < 0) {
        return NULL;
    }

    return JNIConvertor::as_byte_array(env, encryptedContent, resultSize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_decryptContentRSA(JNIEnv *env,
                                                                            jobject obj,
                                                                            jbyteArray jdata,
                                                                            jstring jpass) {
    string pass = JNIConvertor::fromJStringToString(env, jpass);
    string data = JNIConvertor::fromJByteArrayToString(env, jdata);

    unsigned char *clearContent;
    int resultSize = -1;
    try {
        clearContent = SharingV2Crypto::decryptContentRSA(data, pass, resultSize);
    }
    catch (std::exception *e) {}
    catch (std::exception &e) {}
    catch (...) {}

    if (resultSize < 0) {
        return NULL;
    }

    return JNIConvertor::as_byte_array(env, clearContent, resultSize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_decryptContentRsaPkcs1OaepPadding(
        JNIEnv *env,
        jobject obj,
        jbyteArray jdata,
        jstring jpass) {
    string pass = JNIConvertor::fromJStringToString(env, jpass);
    string data = JNIConvertor::fromJByteArrayToString(env, jdata);

    unsigned char *clearContent;
    int resultSize = -1;
    try {
        const char *cipheredData = data.c_str();
        int dataLength = data.size();
        clearContent = RSACipherUtils::rsa_decrypt(cipheredData, pass.c_str(), dataLength, false,
                                                   resultSize);
    }
    catch (std::exception *e) {}
    catch (std::exception &e) {}
    catch (...) {}

    if (resultSize < 0) {
        return NULL;
    }

    return JNIConvertor::as_byte_array(env, clearContent, resultSize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_signContentRSA(JNIEnv *env, jobject obj,
                                                                         jbyteArray jdata,
                                                                         jstring jpass) {
    string pass = JNIConvertor::fromJStringToString(env, jpass);
    string data = JNIConvertor::fromJByteArrayToString(env, jdata);

    unsigned char *signedContent;
    unsigned int resultSize = -1;

    try {
        signedContent = SharingV2Crypto::signContentRSA(data, pass, &resultSize);
    }
    catch (std::exception *e) {}
    catch (std::exception &e) {}
    catch (...) {}

    if ((int) resultSize < 0) {
        return NULL;
    }
    return JNIConvertor::as_byte_array(env, signedContent, (int) resultSize);
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_verifySignatureRSA(JNIEnv *env,
                                                                             jobject obj,
                                                                             jbyteArray jplaintextContent,
                                                                             jbyteArray jsignedContent,
                                                                             jstring jpublicKey) {
    string publicKey = JNIConvertor::fromJStringToString(env, jpublicKey);
    string plaintextContent = JNIConvertor::fromJByteArrayToString(env, jplaintextContent);
    unsigned char *signedContent = JNIConvertor::as_unsigned_char_array(env, jsignedContent);
    int signedDataSize = env->GetArrayLength(jsignedContent);

    bool result;

    try {
        result = SharingV2Crypto::verifySignatureRSA(signedContent, signedDataSize, publicKey,
                                                     plaintextContent);
    }
    catch (std::exception *e) {}
    catch (std::exception &e) {}
    catch (...) {}

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_getKeyForAppData(JNIEnv *env,
                                                                           jobject obj) {
    string key = Utils::get_value();
    return JNIConvertor::fromStringToJString(env, key);
}

JNIEXPORT jobjectArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_generateRSAKeyPair(JNIEnv *env,
                                                                             jobject obj) {
    jobjectArray array = (jobjectArray) env->NewObjectArray(2, env->FindClass("java/lang/String"),
                                                            env->NewStringUTF(""));

    string rsaPublicKey("");
    string rsaPrivateKey("");
    RSACipherUtils::rsa_generateKeyPair(rsaPublicKey, rsaPrivateKey);

    env->SetObjectArrayElement(array, 0, JNIConvertor::fromStringToJString(env, rsaPublicKey));
    env->SetObjectArrayElement(array, 1, JNIConvertor::fromStringToJString(env, rsaPrivateKey));

    return array;
}

JNIEXPORT jobjectArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_generateX25519KeyPair(JNIEnv *env,
                                                                                jobject obj) {
    jobjectArray array = (jobjectArray) env->NewObjectArray(2, env->FindClass("java/lang/String"),
                                                            env->NewStringUTF(""));

    string publicKey("");
    string privateKey("");

    try {
        /* Generate private and public key */
        EVP_PKEY *pkey = NULL;
        EVP_PKEY_CTX *pctx = EVP_PKEY_CTX_new_id(NID_X25519, NULL);
        EVP_PKEY_keygen_init(pctx);
        EVP_PKEY_keygen(pctx, &pkey);
        EVP_PKEY_CTX_free(pctx);

        BIO *tmpBuffer = BIO_new(BIO_s_mem());

        /* Write public key */
        PEM_write_bio_PUBKEY(tmpBuffer, pkey);

        char *tmpResult = NULL;
        long tmpSize = BIO_get_mem_data(tmpBuffer, &tmpResult);
        publicKey = std::string(tmpResult, tmpSize);

        BIO_reset(tmpBuffer);

        /* Write private key  */
        PEM_write_bio_PrivateKey(tmpBuffer, pkey, NULL, NULL, 0, NULL, NULL);

        tmpSize = BIO_get_mem_data(tmpBuffer, &tmpResult);
        privateKey = std::string(tmpResult, tmpSize);

        BIO_free(tmpBuffer);
    } catch (...) {}

    env->SetObjectArrayElement(array, 0, JNIConvertor::fromStringToJString(env, publicKey));
    env->SetObjectArrayElement(array, 1, JNIConvertor::fromStringToJString(env, privateKey));

    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_cryptography_jni_JniWrapperCryptography_deriveX25519SharedSecret(
        JNIEnv *env, jobject thiz,
        jstring privateKey,
        jstring peerPublicKey,
        jbyteArray salt,
        jbyteArray sharedInfo,
        jint derivedKeySize) {
    try {
        string cPrivateKey = JNIConvertor::fromJStringToString(env, privateKey);
        string cPeerPublicKey = JNIConvertor::fromJStringToString(env, peerPublicKey);

        BIO *tmpBuffer = BIO_new(BIO_s_mem());
        const char *data = cPrivateKey.c_str();

        BIO_write(tmpBuffer, data, strlen(data));
        EVP_PKEY *pkey = NULL;
        PEM_read_bio_PrivateKey(tmpBuffer, &pkey, 0, 0);

        data = cPeerPublicKey.c_str();
        BIO_write(tmpBuffer, data, strlen(data));
        EVP_PKEY *peerKey = NULL;
        PEM_read_bio_PUBKEY(tmpBuffer, &peerKey, 0, 0);

        BIO_free(tmpBuffer);

        if (pkey == NULL || peerKey == NULL) return NULL;

        /* Generate shared secret */
        EVP_PKEY_CTX *ctx;
        unsigned char *sharedKey;
        size_t sharedKeyLength;
        ctx = EVP_PKEY_CTX_new(pkey, NULL);

        if (!ctx) {
            /* CTX is empty" */
            return NULL;
        }

        if (EVP_PKEY_derive_init(ctx) <= 0) {
            /* EVP derive initialization failed */
            return NULL;
        }

        if (EVP_PKEY_derive_set_peer(ctx, peerKey) <= 0) {
            /* EVP derive set peer failed */
            return NULL;
        }

        /* Determine buffer length */
        if (EVP_PKEY_derive(ctx, NULL, &sharedKeyLength) <= 0) {
            /* EVP derive failed */
            return NULL;
        }

        sharedKey = (unsigned char *) OPENSSL_malloc(sharedKeyLength);

        if (!sharedKey) {
            /* Malloc failure */
            return NULL;
        }

        if (EVP_PKEY_derive(ctx, sharedKey, &sharedKeyLength) <= 0) {
            /* Shared key derivation failed */
            return NULL;
        }

        EVP_PKEY_CTX_free(ctx);

        /* Derive symmetric key */

        EVP_PKEY_CTX *pctx;
        uint8_t derivedKeyResult[derivedKeySize];
        pctx = EVP_PKEY_CTX_new_id(EVP_PKEY_HKDF, NULL);

        if (EVP_PKEY_derive_init(pctx) <= 0) {
            return NULL;
        }

        if (EVP_PKEY_CTX_set_hkdf_md(pctx, EVP_sha256()) <= 0) {
            return NULL;
        }

        if (EVP_PKEY_CTX_set1_hkdf_salt(pctx, JNIConvertor::as_char_array(env, salt), env->GetArrayLength(salt)) <= 0) {
            return NULL;
        }

        if (EVP_PKEY_CTX_set1_hkdf_key(pctx, sharedKey, sharedKeyLength) <= 0) {
            return NULL;
        }

        if (EVP_PKEY_CTX_add1_hkdf_info(pctx, JNIConvertor::as_char_array(env, sharedInfo), env->GetArrayLength(sharedInfo)) <= 0) {
            return NULL;
        }

        size_t derivedKeyResultLen = sizeof(derivedKeyResult);

        if (EVP_PKEY_derive(pctx, derivedKeyResult, &derivedKeyResultLen) <= 0) {
            return NULL;
        }

        EVP_PKEY_CTX_free(pctx);

        return JNIConvertor::as_byte_array(env, derivedKeyResult, derivedKeySize);
    } catch (...) {
        return NULL;
    }
}
}
