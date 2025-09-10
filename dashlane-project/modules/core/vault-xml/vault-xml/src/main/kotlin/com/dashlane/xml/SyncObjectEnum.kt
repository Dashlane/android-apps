package com.dashlane.xml

interface SyncObjectEnum {
    val value: String

    companion object {
        inline fun <reified T : SyncObjectEnum> getEnumForValue(value: String): T? {
            return T::class.java.enumConstants.firstOrNull { it.value == value }
        }
    }
}