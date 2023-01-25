@file:JvmName("CharSequenceUtils")

package com.dashlane.util

fun CharSequence.containsLowercase() = any(Char::isLowerCase)
fun CharSequence.containsUppercase() = any(Char::isUpperCase)
fun CharSequence.containsLetter() = any(Char::isLetter)
fun CharSequence.containsDigit() = any(Char::isDigit)
fun CharSequence.containsSymbol() = !all(Char::isLetterOrDigit)