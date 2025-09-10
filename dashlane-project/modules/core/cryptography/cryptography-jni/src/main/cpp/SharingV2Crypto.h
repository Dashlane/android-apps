#include <string>

class SharingV2Crypto {
public:
    static unsigned char *
    encryptContentRSA(const std::string &clearContent,
                      const std::string publicKey,
                      int &resultSize);

    static unsigned char *
    decryptContentRSA(const std::string &cipherText,
                      const std::string privateKey,
                      int &resultSize);

    static unsigned char *
    signContentRSA(const std::string &clearContent,
                   const std::string privateKey,
                   unsigned int *resultSize);

    static bool
    verifySignatureRSA(const unsigned char *signedData,
                       int signedDataSize,
                       const std::string &publicKey,
                       const std::string &dataToSign);
};