package com.dashlane.password.generator

data class PasswordGeneratorCriteria(
    val length: Int,
    val digits: Boolean,
    val letters: Boolean,
    val symbols: Boolean,
    val ambiguousChars: Boolean,
)