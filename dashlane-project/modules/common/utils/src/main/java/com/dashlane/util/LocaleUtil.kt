package com.dashlane.util

import java.util.Locale

fun getOsLang() = Locale.getDefault().language.substring(0, 2).lowercase()