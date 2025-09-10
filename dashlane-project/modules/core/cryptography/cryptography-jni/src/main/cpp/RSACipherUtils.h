#include <jni.h>
#include <string>
#include <openssl/rsa.h>

class RSACipherUtils {
public:

    static unsigned char *
    rsa_encrypt(const char *plainText, std::string publicKey, int &plainTextSize, bool useSha256, int &resultSize);

    static unsigned char *
    rsa_decrypt(const char *cipherText, std::string privateKey, int &cipherTextSize, bool useSha256, int &resultSize);

    static unsigned char *
    signRsaSha256(const std::string &dataToSign, const std::string &privateKey, unsigned int *resultSize);

    static bool
    verifyRsaSha256(const unsigned char *signedData, int signedDataSize, const std::string &publicKey,
                    const std::string &dataToSign);

    static unsigned char *
    sha256(const std::string &dataToHash);

    static RSA *get_RSAFromPrivate(const std::string &privateKey);

    /**
     * get_RSA Public Key from both PKCS#1 and PKCS#8
     */
    static RSA *get_RSAFromPublic(const std::string &publicKey);

    static void rsa_generateKeyPair(std::string &publicKey, std::string &privateKey);
};