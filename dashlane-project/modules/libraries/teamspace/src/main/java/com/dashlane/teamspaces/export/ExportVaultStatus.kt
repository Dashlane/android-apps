package com.dashlane.teamspaces.export

import com.dashlane.teamspaces.manager.TeamSpaceAccessor

enum class ExportVaultStatus {
    
    LIMITED,

    
    DISABLED,

    
    COMPLETE,
}

val ExportVaultStatus.isEnable: Boolean
    get() = this != ExportVaultStatus.DISABLED

val TeamSpaceAccessor.exportVaultStatus: ExportVaultStatus
    get() {
        if (!isB2bUser) return ExportVaultStatus.COMPLETE
        return when {
            hasEnforcedTeamSpace -> {
                if (isVaultExportEnabled) ExportVaultStatus.COMPLETE else ExportVaultStatus.DISABLED
            }
            isForcedDomainsEnabled -> ExportVaultStatus.LIMITED
            else -> ExportVaultStatus.COMPLETE
        }
    }