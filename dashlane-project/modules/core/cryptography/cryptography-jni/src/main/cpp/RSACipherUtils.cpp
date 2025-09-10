#include <openssl/buffer.h>
#include <openssl/evp.h>
#include <openssl/asn1.h>
#include <openssl/md5.h>
#include <openssl/des.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/bio.h>
#include <openssl/rand.h>
#include <openssl/engine.h>
#include <openssl/conf.h>
#include <jni.h>
#include <string>
#include <malloc.h>
#include <cstring>
#include "RSACipherUtils.h"
#include "OpenSSL_RSA_extend.h"

const unsigned int RSA_KEYLENGTH = 2048;

unsigned char *
RSACipherUtils::rsa_encrypt(const char *plainText, std::string publicKey, int &plainTextSize,
                            bool useSha256,
                            int &resultSize) {
    if (publicKey == "") {
        return NULL;
    }
    // Init needed buffers

    BIO *tmpBuffer = BIO_new_mem_buf(const_cast<char *>(publicKey.c_str()), publicKey.length());
    if (tmpBuffer == NULL) {
        return NULL;
    }

    // Read public key from PEM format

    RSA *rsaKey = PEM_read_bio_RSAPublicKey(tmpBuffer, NULL, NULL, NULL);
    if (rsaKey == NULL) {
        return NULL;
    }

    // Verify that the message length is less than RSA_size - 41
    // which is the maximum size it can have to be securely encrypted with the OAEP padding.
    int rsaSize = RSA_size(rsaKey);
    if (!(plainTextSize < rsaSize - 41)) {
        return NULL;
    }

    unsigned char *cipherText = (unsigned char *) malloc(rsaSize + 1);;
    memset(cipherText, 0, rsaSize + 1);

    if (useSha256) {
        resultSize = KW_RSA_public_encrypt_RSA_PKCS1_OAEP_PADDING_SHA256(plainTextSize,
                                                                         (const unsigned char *) plainText,
                                                                         cipherText,
                                                                         rsaKey);
    } else {
        // Encrypt plainText with the public key.
        // Use RSA_PKCS1_OAEP_PADDING (EME-OAEP as defined in PKCS #1 v2.0 with SHA-1, MGF1 and an empty encoding parameter)
        // as recommended here https://www.openssl.org/docs/crypto/RSA_public_encrypt.html
        resultSize = RSA_public_encrypt(plainTextSize, (const unsigned char *) plainText,
                                        cipherText, rsaKey,
                                        RSA_PKCS1_OAEP_PADDING);
    }

    // Free buffers
    RSA_free(rsaKey);
    BIO_free(tmpBuffer);

    // Return result
    return cipherText;
}


unsigned char *
RSACipherUtils::rsa_decrypt(const char *cipherText, std::string privateKey, int &cipherTextSize,
                            bool useSha256,
                            int &resultSize) {
    // Init needed buffers
    char *keyData = new char[privateKey.size()];
    memcpy(keyData, privateKey.c_str(), privateKey.size());

    BIO *tmpBuffer = BIO_new_mem_buf(keyData, privateKey.size());
    if (tmpBuffer == NULL) {
        return NULL;
    }

    // Read private key from PEM format
    RSA *rsaKey = PEM_read_bio_RSAPrivateKey(tmpBuffer, NULL, NULL, NULL);
    if (rsaKey == NULL) {
        return NULL;
    }
    // RSA_size -41 is the maximum message size accepted
    // with the padding that we use
    int rsaSize = RSA_size(rsaKey);

    unsigned char *plainText = (unsigned char *) malloc(rsaSize + 1);
    memset(plainText, 0, rsaSize + 1);

    if (useSha256) {
        resultSize = KW_RSA_private_decrypt_RSA_PKCS1_OAEP_PADDING_SHA256(cipherTextSize,
                                                                          (const unsigned char *) cipherText,
                                                                          plainText, rsaKey);
    } else {
        // Decrypt ciphertext with the private key.
        // Use RSA_PKCS1_OAEP_PADDING as recommended here https://www.openssl.org/docs/crypto/RSA_public_encrypt.html
        resultSize = RSA_private_decrypt(cipherTextSize, (const unsigned char *) cipherText,
                                         plainText, rsaKey, RSA_PKCS1_OAEP_PADDING);
    }

    // Free buffers
    delete[] keyData;
    RSA_free(rsaKey);
    BIO_free(tmpBuffer);
    // return result
    return plainText;
}

unsigned char *
RSACipherUtils::signRsaSha256(const std::string &dataToSign, const std::string &privateKey,
                              unsigned int *resultSize) {
    try {
        unsigned char *digest(sha256(dataToSign));

        RSA *privateKey_rsa = get_RSAFromPrivate(privateKey);

        if (privateKey_rsa == NULL) return NULL;

        unsigned char *signedMessage = (unsigned char *) malloc(5000);
        memset(signedMessage, 0, 5000);

        int successRSASign = RSA_sign(NID_sha256, digest, SHA256_DIGEST_LENGTH, signedMessage,
                                      resultSize,
                                      privateKey_rsa);
        RSA_free(privateKey_rsa);

        if (successRSASign != 1) return NULL;

        return signedMessage;
    } catch (...) { return NULL; }
}

bool
RSACipherUtils::verifyRsaSha256(const unsigned char *signedData, int signedDataSize,
                                const std::string &publicKey,
                                const std::string &dataToSign) {
    try {

        unsigned char *digest(sha256(dataToSign));

        RSA *publicKey_rsa = get_RSAFromPublic(publicKey);

        if (publicKey_rsa == NULL) return false;

        int successRSAVerify = RSA_verify(NID_sha256, digest, SHA256_DIGEST_LENGTH, signedData,
                                          signedDataSize,
                                          publicKey_rsa);
        RSA_free(publicKey_rsa);

        return successRSAVerify == 1;

    } catch (...) { return false; }
}


unsigned char *RSACipherUtils::sha256(const std::string &dataToHash) {
    try {
        unsigned char *digest = new unsigned char[SHA256_DIGEST_LENGTH]();
        unsigned char *successSHA256 = SHA256((const unsigned char *) (dataToHash.c_str()),
                                              dataToHash.size(),
                                              digest);
        if (successSHA256 == 0) return NULL;

        return digest;

    } catch (...) { return NULL; }
}

RSA *RSACipherUtils::get_RSAFromPrivate(const std::string &privateKey) {
    try {
        const char *mKey = privateKey.c_str();

        BIO *bo = BIO_new(BIO_s_mem());
        BIO_write(bo, mKey, strlen(mKey));

        EVP_PKEY *pkey = 0;
        PEM_read_bio_PrivateKey(bo, &pkey, 0, 0);

        if (pkey == NULL) return NULL;

        BIO_free(bo);

        RSA *rsa = EVP_PKEY_get1_RSA(pkey);
        EVP_PKEY_free(pkey);

        if (rsa == NULL) return NULL;
        return rsa;
    } catch (...) { return NULL; }
}

RSA *RSACipherUtils::get_RSAFromPublic(const std::string &publicKeyData) {
    try {
        RSA *rsa = RSA_new();

        const char *publicKey = const_cast<char *>(publicKeyData.c_str());
        int publicKeySize = publicKeyData.length();

        // Read PKCS#1 by PEM_read_bio_RSAPublicKey
        BIO *bioBufPKCS1 = BIO_new_mem_buf(publicKey, publicKeySize);
        if (bioBufPKCS1 == NULL) {
            return NULL;
        }
        RSA *rsaPublicKey = PEM_read_bio_RSAPublicKey(bioBufPKCS1, &rsa, 0, NULL);
        BIO_free(bioBufPKCS1);

        if (rsaPublicKey != NULL) {
            return rsa;
        }

        // Read PKCS#8 by PEM_read_bio_RSA_PUBKEY
        BIO *bioBufPKCS8 = BIO_new_mem_buf(publicKey, publicKeySize);
        if (bioBufPKCS8 == NULL) {
            return NULL;
        }
        RSA *rsaKey = PEM_read_bio_RSA_PUBKEY(bioBufPKCS8, NULL, NULL, NULL);
        BIO_free(bioBufPKCS8);
        return rsaKey;
    } catch (...) { return NULL; }
}

void RSACipherUtils::rsa_generateKeyPair(std::string &publicKey, std::string &privateKey) {
    try {
        BIGNUM *e = NULL;
        RSA *rsaStruct = NULL;

        // Init needed structs
        rsaStruct = RSA_new();
        e = BN_new();

        // As we use RSA_PKCS1_OAEP_PADDING (EME-OAEP as defined in PKCS #1 v2.0 with SHA-1, MGF1 and an empty encoding parameter),
        // there is no known security drawback of using a small public exponent (i.e. 3) instead of 65537.
        // There is however a big performance advantage ( ~8.5x at key generation).
        BN_set_word(e, 3);

        // RSA keys must be larger than 1024 bits and are recommended to be at least 2048-3072 bits long.
        // We use 2048 bits keys for now.
        int rsaKeyLength = RSA_KEYLENGTH;

        if (!RSA_generate_key_ex(rsaStruct, rsaKeyLength, e, NULL)) {
            return;
        }

        // Free this as not needed anymore
        BN_free(e);
        e = NULL;

        // Init a new buffer
        BIO *tmpBuffer = BIO_new(BIO_s_mem());

        // Write the public key in PEM format
        PEM_write_bio_RSAPublicKey(tmpBuffer, rsaStruct);

        // Extract the public key to a string
        char *tmpResult = NULL;
        long tmpSize = BIO_get_mem_data(tmpBuffer, &tmpResult);
        publicKey = std::string(tmpResult, tmpSize);

        // Reset buffer
        BIO_reset(tmpBuffer);
        // Write the private key in PEM format
        PEM_write_bio_RSAPrivateKey(tmpBuffer, rsaStruct, NULL, NULL, 0, 0, NULL);

        // Extract the private key to a string
        tmpSize = BIO_get_mem_data(tmpBuffer, &tmpResult);
        privateKey = std::string(tmpResult, tmpSize);

        // Free the last remaining buffers
        BIO_free(tmpBuffer);
        RSA_free(rsaStruct);
    }
    catch (...) {}
}
