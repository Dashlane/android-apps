#include <jni.h>

#include <sodium/crypto_kx.h>
#include <sodium/crypto_secretstream_xchacha20poly1305.h>
#include "sodium/crypto_secretbox_xchacha20poly1305.h"
#include "sodium/crypto_secretbox.h"
#include "../../../../cryptography-jni/src/main/cpp/JNIConvertor.h"
#include "sodium/crypto_secretbox_xchacha20poly1305.h"
#include "sodium/randombytes.h"
#include "sodium/crypto_generichash.h"
#include "sodium/utils.h"


extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1kx_1keypair(JNIEnv *env,
                                                           jobject thiz,
                                                           jbyteArray public_key,
                                                           jbyteArray secret_key) {
    jbyte *pk = env->GetByteArrayElements(public_key, nullptr);
    jbyte *sk = env->GetByteArrayElements(secret_key, nullptr);

    if (pk == nullptr || sk == nullptr) {
        return false;
    }

    bool success = crypto_kx_keypair((unsigned char *) pk, (unsigned char *) sk) == 0;

    env->ReleaseByteArrayElements(public_key, pk, 0);
    env->ReleaseByteArrayElements(secret_key, sk, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1kx_1client_1session_1keys(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jbyteArray rx,
                                                                         jbyteArray tx,
                                                                         jbyteArray public_key,
                                                                         jbyteArray secret_key,
                                                                         jbyteArray peer_public_key) {
    jbyte *r = env->GetByteArrayElements(rx, nullptr);
    jbyte *t = env->GetByteArrayElements(tx, nullptr);
    jbyte *pk = env->GetByteArrayElements(public_key, nullptr);
    jbyte *sk = env->GetByteArrayElements(secret_key, nullptr);
    jbyte *ppk = env->GetByteArrayElements(peer_public_key, nullptr);

    if (r == nullptr || t == nullptr || pk == nullptr || sk == nullptr || ppk == nullptr) {
        return false;
    }

    bool success = crypto_kx_client_session_keys((unsigned char *) r,
                                                 (unsigned char *) t,
                                                 (unsigned char *) pk,
                                                 (unsigned char *) sk,
                                                 (unsigned char *) ppk) == 0;

    env->ReleaseByteArrayElements(rx, r, 0);
    env->ReleaseByteArrayElements(tx, t, 0);
    env->ReleaseByteArrayElements(public_key, pk, 0);
    env->ReleaseByteArrayElements(secret_key, sk, 0);
    env->ReleaseByteArrayElements(peer_public_key, ppk, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1kx_1server_1session_1keys(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jbyteArray rx,
                                                                         jbyteArray tx,
                                                                         jbyteArray public_key,
                                                                         jbyteArray secret_key,
                                                                         jbyteArray client_public_key) {
    jbyte *r = env->GetByteArrayElements(rx, nullptr);
    jbyte *t = env->GetByteArrayElements(tx, nullptr);
    jbyte *pk = env->GetByteArrayElements(public_key, nullptr);
    jbyte *sk = env->GetByteArrayElements(secret_key, nullptr);
    jbyte *cpk = env->GetByteArrayElements(client_public_key, nullptr);

    if (r == nullptr || t == nullptr || pk == nullptr || sk == nullptr || cpk == nullptr) {
        return false;
    }

    bool success = crypto_kx_server_session_keys((unsigned char *) r,
                                                 (unsigned char *) t,
                                                 (unsigned char *) pk,
                                                 (unsigned char *) sk,
                                                 (unsigned char *) cpk) == 0;

    env->ReleaseByteArrayElements(rx, r, 0);
    env->ReleaseByteArrayElements(tx, t, 0);
    env->ReleaseByteArrayElements(public_key, pk, 0);
    env->ReleaseByteArrayElements(secret_key, sk, 0);
    env->ReleaseByteArrayElements(client_public_key, cpk, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretstream_1xchacha20poly1305_1init_1push(
        JNIEnv *env,
        jobject thiz,
        jbyteArray state,
        jbyteArray header,
        jbyteArray key) {
    jbyte *s = env->GetByteArrayElements(state, nullptr);
    jbyte *h = env->GetByteArrayElements(header, nullptr);
    jbyte *k = env->GetByteArrayElements(key, nullptr);

    if (s == nullptr || h == nullptr || k == nullptr) {
        return false;
    }

    bool success = crypto_secretstream_xchacha20poly1305_init_push(
            (crypto_secretstream_xchacha20poly1305_state *) s,
            (unsigned char *) h,
            (unsigned char *) k) == 0;

    env->ReleaseByteArrayElements(state, s, 0);
    env->ReleaseByteArrayElements(header, h, 0);
    env->ReleaseByteArrayElements(key, k, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretstream_1xchacha20poly1305_1push(
        JNIEnv *env, jobject thiz, jbyteArray state, jbyteArray encrypted, jbyteArray message) {
    jbyte *s = env->GetByteArrayElements(state, nullptr);
    jbyte *e = env->GetByteArrayElements(encrypted, nullptr);
    jbyte *m = env->GetByteArrayElements(message, nullptr);

    if (s == nullptr || e == nullptr || m == nullptr) {
        return false;
    }

    bool success = crypto_secretstream_xchacha20poly1305_push(
            (crypto_secretstream_xchacha20poly1305_state *) s,
            (unsigned char *) e,
            nullptr,
            (unsigned char *) m,
            (unsigned long long int) env->GetArrayLength(message),
            nullptr,
            0,
            crypto_secretstream_xchacha20poly1305_TAG_MESSAGE) == 0;

    env->ReleaseByteArrayElements(state, s, 0);
    env->ReleaseByteArrayElements(encrypted, e, 0);
    env->ReleaseByteArrayElements(message, m, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretstream_1xchacha20poly1305_1init_1pull(
        JNIEnv *env, jobject thiz, jbyteArray state, jbyteArray header, jbyteArray key) {
    jbyte *s = env->GetByteArrayElements(state, nullptr);
    jbyte *h = env->GetByteArrayElements(header, nullptr);
    jbyte *k = env->GetByteArrayElements(key, nullptr);

    if (s == nullptr || h == nullptr || k == nullptr) {
        return false;
    }

    bool success = crypto_secretstream_xchacha20poly1305_init_pull(
            (crypto_secretstream_xchacha20poly1305_state *) s,
            (unsigned char *) h,
            (unsigned char *) k) == 0;

    env->ReleaseByteArrayElements(state, s, 0);
    env->ReleaseByteArrayElements(header, h, 0);
    env->ReleaseByteArrayElements(key, k, 0);

    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretstream_1xchacha20poly1305_1pull(
        JNIEnv *env, jobject thiz, jbyteArray state, jbyteArray message, jbyteArray encrypted) {
    jbyte *s = env->GetByteArrayElements(state, nullptr);
    jbyte *e = env->GetByteArrayElements(encrypted, nullptr);
    jbyte *m = env->GetByteArrayElements(message, nullptr);

    if (s == nullptr || e == nullptr || m == nullptr) {
        return false;
    }

    bool success = crypto_secretstream_xchacha20poly1305_pull(
            (crypto_secretstream_xchacha20poly1305_state *) s,
            (unsigned char *) m,
            nullptr,
            nullptr,
            (unsigned char *) e,
            (unsigned long long int) env->GetArrayLength(encrypted),
            nullptr,
            0) == 0;

    env->ReleaseByteArrayElements(state, s, 0);
    env->ReleaseByteArrayElements(encrypted, e, 0);
    env->ReleaseByteArrayElements(message, m, 0);

    return success;
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretbox_1easy(JNIEnv *env,
                                                               jobject thiz,
                                                               jbyteArray message,
                                                               jbyteArray nonce,
                                                               jbyteArray key) {
    int messageSize = env->GetArrayLength(message);
    int cipherTextSize = crypto_secretbox_MACBYTES + messageSize;
    unsigned char c[cipherTextSize];
    unsigned char *m = JNIConvertor::as_unsigned_char_array(env, message);
    unsigned char *n = JNIConvertor::as_unsigned_char_array(env, nonce);
    unsigned char *k = JNIConvertor::as_unsigned_char_array(env, key);

    int success = crypto_secretbox_easy(c, m, messageSize, n, k);

    if (success != 0) {
        return nullptr;
    }

    return JNIConvertor::as_byte_array(env, c, cipherTextSize);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1secretbox_1open_1easy(JNIEnv *env,
                                                                     jobject thiz,
                                                                     jbyteArray cipherText,
                                                                     jbyteArray nonce,
                                                                     jbyteArray key) {

    int cipherTextSize = env->GetArrayLength(cipherText);
    int messageSize = cipherTextSize - crypto_secretbox_MACBYTES;
    unsigned char m[messageSize];
    unsigned char *c = JNIConvertor::as_unsigned_char_array(env, cipherText);
    unsigned char *n = JNIConvertor::as_unsigned_char_array(env, nonce);
    unsigned char *k = JNIConvertor::as_unsigned_char_array(env, key);

    int success = crypto_secretbox_open_easy(m, c, cipherTextSize, n, k);

    if (success != 0) {
        return nullptr;
    }

    return JNIConvertor::as_byte_array(env, m, messageSize);
}


JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_randombytes_1buf(JNIEnv *env,
                                                        jobject thiz,
                                                        jint size) {

    unsigned char buf[size];

    randombytes_buf(buf, sizeof buf);

    return JNIConvertor::as_byte_array(env, buf, size);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1generichash__I_3BJ_3BI(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jint outlen,
                                                                         jbyteArray input,
                                                                         jlong inlen,
                                                                         jbyteArray key,
                                                                         jint keylen) {

    uint8_t hash[outlen];
    unsigned char *in = JNIConvertor::as_unsigned_char_array(env, input);
    unsigned char *k = JNIConvertor::as_unsigned_char_array(env, key);

    crypto_generichash(hash, outlen, in, inlen, k, keylen);

    return JNIConvertor::as_byte_array(env, hash, outlen);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_crypto_1generichash__I_3BJ(JNIEnv *env,
                                                                 jobject thiz,
                                                                 jint outlen,
                                                                 jbyteArray input,
                                                                 jlong inlen) {
    uint8_t hash[outlen];
    unsigned char *in = JNIConvertor::as_unsigned_char_array(env, input);

    crypto_generichash(hash, outlen, in, inlen, nullptr, 0);

    return JNIConvertor::as_byte_array(env, hash, outlen);
}

JNIEXPORT jboolean JNICALL
Java_com_dashlane_sodium_jni_SodiumJni_sodium_1memcmp(JNIEnv *env,
                                                      jobject thiz,
                                                      jbyteArray key1,
                                                      jbyteArray key2) {

    unsigned char *k1 = JNIConvertor::as_unsigned_char_array(env, key1);
    unsigned char *k2 = JNIConvertor::as_unsigned_char_array(env, key2);

    bool success = sodium_memcmp(k1, k2, sizeof k1) == 0;

    return success;
}
}
