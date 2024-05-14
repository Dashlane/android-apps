@file:JvmName("IntUtils")

package com.dashlane.util

fun flagsOf(vararg flags: Int) = flags.reduce(Int::or)
infix fun Int.hasFlag(flag: Int) = this and flag != 0

fun Int.toBoolean() = this != 0
fun Boolean.toInt() = if (this) 1 else 0
