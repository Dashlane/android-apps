package com.dashlane.cryptography

import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Base64Test {
    @Test
    fun encodeBase64() {
        assertEquals("", "".encodeToByteArray().encodeBase64ToString())
        assertEquals("AA==", "\u0000".encodeToByteArray().encodeBase64ToString())
        assertEquals("AAA=", "\u0000\u0000".encodeToByteArray().encodeBase64ToString())
        assertEquals("AAAA", "\u0000\u0000\u0000".encodeToByteArray().encodeBase64ToString())
        assertEquals(
            "SG93IG1hbnkgbGluZXMgb2YgY29kZSBhcmUgdGhlcmU/ICdib3V0IDIgbWlsbGlvbi4=",
            "How many lines of code are there? 'bout 2 million.".encodeToByteArray().encodeBase64ToString(),
        )
    }

    @Test
    fun ignoreUnnecessaryPadding() {
        assertEquals("", "====".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", "AAAA====".decodeBase64ToByteArrayOrNull()!!.decodeToString())
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun decodeBase64ToByteArrayOrNull() {
        assertEquals("", "".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals(null, "/===".decodeBase64ToByteArrayOrNull()) 
        assertEquals("ff".decodeHex(), "
        assertEquals("ff".decodeHex(), "__==".decodeBase64ToByteArrayOrNull()!!.toByteString())
        assertEquals("ffff".decodeHex(), "
        assertEquals("ffff".decodeHex(), "___=".decodeBase64ToByteArrayOrNull()!!.toByteString())
        assertEquals("ffffff".decodeHex(), "
        assertEquals("ffffff".decodeHex(), "____".decodeBase64ToByteArrayOrNull()!!.toByteString())
        assertEquals("ffffffffffff".decodeHex(), "
        assertEquals("ffffffffffff".decodeHex(), "________".decodeBase64ToByteArrayOrNull()!!.toByteString())
        assertEquals(
            "What's to be scared about? It's just a little hiccup in the power...",
            "V2hhdCdzIHRvIGJlIHNjYXJlZCBhYm91dD8gSXQncyBqdXN0IGEgbGl0dGxlIGhpY2N1cCBpbiB0aGUgcG93ZXIuLi4=".decodeBase64ToByteArrayOrNull()!!.decodeToString(),
        )
        
        assertEquals("ffffff".decodeHex(), "__
    }

    @Test
    fun decodeBase64WithWhitespace() {
        assertEquals("\u0000\u0000\u0000", " AA AA ".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", " AA A\r\nA ".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", "AA AA".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", " AA AA ".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", " AA A\r\nA ".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("\u0000\u0000\u0000", "A    AAA".decodeBase64ToByteArrayOrNull()!!.decodeToString())
        assertEquals("", "    ".decodeBase64ToByteArrayOrNull()!!.decodeToString())
    }
}