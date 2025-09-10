#include <string>
#include "SharingV2Crypto.h"
#include "RSACipherUtils.h"

unsigned char *
SharingV2Crypto::encryptContentRSA(const std::string &clearContent,
                                   std::string publicKey,
                                   int &resultSize) {
    int dataLength = clearContent.size();
    return RSACipherUtils::rsa_encrypt(clearContent.c_str(), publicKey, dataLength, true, resultSize);
}

unsigned char *
SharingV2Crypto::decryptContentRSA(const std::string &cipherText,
                                   std::string privateKey,
                                   int &resultSize) {
    const char *cipheredData = cipherText.c_str();
    int dataLength = cipherText.size();
    return RSACipherUtils::rsa_decrypt(cipheredData, privateKey.c_str(), dataLength, true, resultSize);
}

unsigned char *
SharingV2Crypto::signContentRSA(const std::string &clearContent,
                                const std::string privateKey,
                                unsigned int *resultSize) {
    return RSACipherUtils::signRsaSha256(clearContent, privateKey.c_str(), resultSize);
}

bool
SharingV2Crypto::verifySignatureRSA(const unsigned char *signedData,
                                    int signedDataSize,
                                    const std::string &publicKey,
                                    const std::string &dataToSign) {
    return RSACipherUtils::verifyRsaSha256(signedData, signedDataSize, publicKey, dataToSign);
}
