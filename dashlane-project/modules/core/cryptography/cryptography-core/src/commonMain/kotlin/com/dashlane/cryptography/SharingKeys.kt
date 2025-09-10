package com.dashlane.cryptography

data class SharingKeys(
    val public: Public,
    val private: Private,
) {
    constructor(publicKey: String, privateKey: String) : this(Public(publicKey), Private(privateKey))

    data class Public(val value: String)

    data class Private(val value: String)
}