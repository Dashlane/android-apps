package com.dashlane.abtesting

open class AbTest(val name: String) {

    companion object {
        val VARIANT_CONTROL = Variant("controlGroup")
    }
}

data class Variant(val name: String)
