package com.dashlane.cryptography

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class ObfuscatedByteArrayUtf8Test() {
    @ParameterizedTest
    @MethodSource("data")
    fun encodeDecodeString(s: String) {
        val obfuscated = s.encodeUtf8ToObfuscated()
        val string = obfuscated.decodeUtf8ToString()

        assertEquals(s, string)
    }

    @ParameterizedTest
    @MethodSource("data")
    fun encodeDecodeCharArray(s: String) {
        val chars = s.toCharArray()

        val obfuscated = chars.encodeUtf8ToObfuscated()
        val charArray = obfuscated.decodeUtf8ToCharArray()

        assertEquals(chars.concatToString(), charArray.concatToString())
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Arguments.of(""),
            Arguments.of("  "),
            Arguments.of("A"),
            Arguments.of("ABC"),
            Arguments.of("tvnow.de"),
            Arguments.of("LJgCZQtnmMS2hAk8"),
            Arguments.of("RrDGEJysHVBL40okfxRILeyUlrf2YVAD"),
            Arguments.of(" Azert  y12(%^'~`ЂT"),
            Arguments.of("Lorem ipsum dolor sit amet."),
            Arguments.of("تم تكوين 1 فقرة، 5 كلمة، 27 بايت من نص لوريم إيبسوم"),
            Arguments.of("生成1段落，5单词，27字节的"),
        )
    }
}