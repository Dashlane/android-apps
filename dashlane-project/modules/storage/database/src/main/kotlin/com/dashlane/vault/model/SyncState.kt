package com.dashlane.vault.model

enum class SyncState(val code: String) {
    IN_SYNC_MODIFIED("inSync_modified"),
    IN_SYNC_DELETED("inSync_deleted"),
    DELETED("Deleted"),
    MODIFIED("Modified"),
    SYNCED("Synced"),
    ;

    companion object {
        fun fromCode(code: String): SyncState? {
            return when (code) {
                IN_SYNC_MODIFIED.code -> IN_SYNC_MODIFIED
                IN_SYNC_DELETED.code -> IN_SYNC_DELETED
                DELETED.code -> DELETED
                MODIFIED.code -> MODIFIED
                SYNCED.code -> SYNCED
                else -> null
            }
        }
    }
}