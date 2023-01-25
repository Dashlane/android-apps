package com.dashlane.ui.screens.fragments

import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogCode80



object SharingUlHelper {

    @JvmStatic
    fun createUsageLogCode75(type: String, subtype: String, action: String) =
        UsageLogCode75(
            type = type,
            subtype = subtype,
            action = action
        )

    @JvmStatic
    fun createUsageLogCode80(
        type: UsageLogCode80.Type?,
        from: UsageLogCode80.From?,
        action: UsageLogCode80.Action?,
        nbCredentials: Int?,
        nbSecureNotes: Int?
    ) = createUsageLogCode80(type, from, action, nbCredentials, nbSecureNotes, null, null, null, null)

    @JvmStatic
    fun createUsageLogCode80(
        type: UsageLogCode80.Type?,
        from: UsageLogCode80.From?,
        action: UsageLogCode80.Action?,
        nbCredentials: Int?,
        nbSecureNotes: Int?,
        nbGroups: Int?,
        nbUsers: Int?,
        checkCreateGroup: Boolean?,
        permission: Boolean?
    ) =
        UsageLogCode80(
            type = type,
            from = from,
            action = action,
            nbCredentials = nbCredentials,
            nbSecureNotes = nbSecureNotes,
            nbGroups = nbGroups,
            nbUsers = nbUsers,
            checkCreateGroup = checkCreateGroup,
            permission = permission
        )
}