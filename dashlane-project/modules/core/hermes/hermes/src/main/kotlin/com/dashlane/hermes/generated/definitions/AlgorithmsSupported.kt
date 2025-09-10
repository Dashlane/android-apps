package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AlgorithmsSupported(override val code: String) : TrackingLog.Enum {
    A_128_GCM(
        code = "a_128_gcm",
    ),
    A_128_KW(
        code = "a_128_kw",
    ),
    A_192_GCM(
        code = "a_192_gcm",
    ),
    A_192_KW(
        code = "a_192_kw",
    ),
    A_256_GCM(
        code = "a_256_gcm",
    ),
    A_256_KW(
        code = "a_256_kw",
    ),
    AES_CCM_16_128_128(
        code = "aes_ccm_16_128_128",
    ),
    AES_CCM_16_128_256(
        code = "aes_ccm_16_128_256",
    ),
    AES_CCM_16_64_128(
        code = "aes_ccm_16_64_128",
    ),
    AES_CCM_16_64_256(
        code = "aes_ccm_16_64_256",
    ),
    AES_CCM_64_128_128(
        code = "aes_ccm_64_128_128",
    ),
    AES_CCM_64_128_256(
        code = "aes_ccm_64_128_256",
    ),
    AES_CCM_64_64_128(
        code = "aes_ccm_64_64_128",
    ),
    AES_CCM_64_64_256(
        code = "aes_ccm_64_64_256",
    ),
    AES_MAC_128_128(
        code = "aes_mac_128_128",
    ),
    AES_MAC_128_64(
        code = "aes_mac_128_64",
    ),
    AES_MAC_256_128(
        code = "aes_mac_256_128",
    ),
    AES_MAC_256_64(
        code = "aes_mac_256_64",
    ),
    CHA_CHA_20_POLY_1305(
        code = "cha_cha_20_poly_1305",
    ),
    DIRECT(
        code = "direct",
    ),
    DIRECT_HKDF_AES_128(
        code = "direct_hkdf_aes_128",
    ),
    DIRECT_HKDF_AES_256(
        code = "direct_hkdf_aes_256",
    ),
    DIRECT_HKDF_SHA_256(
        code = "direct_hkdf_sha_256",
    ),
    DIRECT_HKDF_SHA_512(
        code = "direct_hkdf_sha_512",
    ),
    ECDH_ES_A128_KW(
        code = "ecdh_es_a128_kw",
    ),
    ECDH_ES_A192_KW(
        code = "ecdh_es_a192_kw",
    ),
    ECDH_ES_A256_KW(
        code = "ecdh_es_a256_kw",
    ),
    ECDH_ES_HKDF_256(
        code = "ecdh_es_hkdf_256",
    ),
    ECDH_ES_HKDF_512(
        code = "ecdh_es_hkdf_512",
    ),
    ECDH_SS_A128_KW(
        code = "ecdh_ss_a128_kw",
    ),
    ECDH_SS_A192_KW(
        code = "ecdh_ss_a192_kw",
    ),
    ECDH_SS_A256_KW(
        code = "ecdh_ss_a256_kw",
    ),
    ECDH_SS_HKDF_256(
        code = "ecdh_ss_hkdf_256",
    ),
    ECDH_SS_HKDF_512(
        code = "ecdh_ss_hkdf_512",
    ),
    ED_DSA(
        code = "ed_dsa",
    ),
    ES_256(
        code = "es_256",
    ),
    ES_256_K(
        code = "es_256_k",
    ),
    ES_384(
        code = "es_384",
    ),
    ES_512(
        code = "es_512",
    ),
    HMAC_256_256(
        code = "hmac_256_256",
    ),
    HMAC_256_64(
        code = "hmac_256_64",
    ),
    HMAC_384_384(
        code = "hmac_384_384",
    ),
    HMAC_512_512(
        code = "hmac_512_512",
    ),
    HSS_LMS(
        code = "hss_lms",
    ),
    IV_GENERATION(
        code = "iv_generation",
    ),
    PS_256(
        code = "ps_256",
    ),
    PS_384(
        code = "ps_384",
    ),
    PS_512(
        code = "ps_512",
    ),
    RESERVED(
        code = "reserved",
    ),
    RS_1(
        code = "rs_1",
    ),
    RS_256(
        code = "rs_256",
    ),
    RS_384(
        code = "rs_384",
    ),
    RS_512(
        code = "rs_512",
    ),
    RSAES_OAEP_WITH_RFC_8017(
        code = "rsaes_oaep_with_rfc_8017",
    ),
    RSAES_OAEP_WITH_SHA_256(
        code = "rsaes_oaep_with_sha_256",
    ),
    RSAES_OAEP_WITH_SHA_512(
        code = "rsaes_oaep_with_sha_512",
    ),
    SHA_1(
        code = "sha_1",
    ),
    SHA_256(
        code = "sha_256",
    ),
    SHA_256_64(
        code = "sha_256_64",
    ),
    SHA_384(
        code = "sha_384",
    ),
    SHA_512(
        code = "sha_512",
    ),
    SHA_512_256(
        code = "sha_512_256",
    ),
    SHAKE_128(
        code = "shake_128",
    ),
    SHAKE_256(
        code = "shake_256",
    ),
    UNASSIGNED(
        code = "unassigned",
    ),
    WALNUT_DSA(
        code = "walnut_dsa",
    ),
}