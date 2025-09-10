/**
 * RSA_private_decrypt and RSA_public_encrypt
 * with RSA_PKCS1_OAEP_PADDING using SHA256
 * Default implementation of OpenSSL is SHA1
 *
 * Adapted from openSSL version 1.1.1b (openssl-OpenSSL_1_1_1b/crypto/rsa/rsa_pmeth.c)
 * Uses RSA_padding_add_PKCS1_OAEP_mgf1 instead of RSA_padding_add_PKCS1_OAEP
 *
 * @see static int pkey_rsa_encrypt(EVP_PKEY_CTX *ctx,
 *                                  unsigned char *out, size_t *outlen,
 *                                  const unsigned char *in, size_t inlen)
 * @see static int pkey_rsa_decrypt(EVP_PKEY_CTX *ctx,
 *                                  unsigned char *out, size_t *outlen,
 *                                  const unsigned char *in, size_t inlen)
 *
 */
static int KW_RSA_private_decrypt_RSA_PKCS1_OAEP_PADDING_SHA256(int flen, const unsigned char *from,
                                                                unsigned char *to, RSA *rsa) {
    int ret, i;
    int rsaSize = RSA_size(rsa);

    unsigned char *buf = (unsigned char *) malloc(rsaSize);
    memset(buf, 0, rsaSize);

    ret = RSA_private_decrypt(flen, from, buf, rsa, RSA_NO_PADDING);
    if (ret <= 0)
        return ret;
    for (i = 0; i < ret; i++) {
        if (buf[i])
            break;
    }
    ret = RSA_padding_check_PKCS1_OAEP_mgf1(to, ret, buf + i,
                                            ret - i, ret,
                                            NULL, 0, EVP_sha256(), NULL);
    return ret;
}

static int KW_RSA_public_encrypt_RSA_PKCS1_OAEP_PADDING_SHA256(int flen, const unsigned char *from,
                                                               unsigned char *to, RSA *rsa) {
    int ret;
    int rsaSize = RSA_size(rsa);

    unsigned char *buf = (unsigned char *) malloc(rsaSize);
    memset(buf, 0, rsaSize);

    if (!RSA_padding_add_PKCS1_OAEP_mgf1(buf, rsaSize,
                                         from, flen,
                                         NULL, 0, EVP_sha256(), NULL)) {
        return -1;
    }
    ret = RSA_public_encrypt(rsaSize, buf, to, rsa, RSA_NO_PADDING);
    return ret;
}
