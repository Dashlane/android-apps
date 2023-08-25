package com.dashlane.followupnotification.domain

import com.dashlane.util.clipboard.vault.CopyField

data class FollowUpNotification(
    val id: String,
    val vaultItemId: String,
    val type: FollowUpNotificationsTypes,
    val name: String,
    val fields: List<Field>,
    val isItemProtected: Boolean,
    val itemDomain: String?
) {
    data class Field(
        val name: String,
        val label: String,
        val content: FieldContent
    ) {
        constructor(
            copyField: CopyField,
            content: FieldContent
        ) : this(
            name = copyField.name,
            label = copyField.name,
            content = content
        )
    }

    sealed class FieldContent(val displayValue: String, val needsUnlock: Boolean) {
        class ClearContent(value: String) : FieldContent(value, false)
        class ObfuscatedContent(value: String) : FieldContent(value, true)

        override fun equals(other: Any?): Boolean {
            if (other !is FieldContent) return false
            return displayValue == other.displayValue && needsUnlock == other.needsUnlock
        }

        override fun hashCode(): Int {
            var result = displayValue.hashCode()
            result = 31 * result + needsUnlock.hashCode()
            return result
        }
    }
}