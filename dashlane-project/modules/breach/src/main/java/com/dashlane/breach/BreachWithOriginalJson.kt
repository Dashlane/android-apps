package com.dashlane.breach

data class BreachWithOriginalJson(val breach: Breach, val json: String, val passwords: List<String>? = null)