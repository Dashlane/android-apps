package com.dashlane.teamspaces.manager

import androidx.fragment.app.FragmentActivity
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.model.Teamspace

private const val ENFORCEMENT_DISABLED = "disabled"

interface TeamspaceAccessor {
    var current: Teamspace?
    val all: List<Teamspace>
    val revokedAndDeclinedSpaces: List<Teamspace>
    fun isCurrent(id: String): Boolean
    fun get(id: String): Teamspace?

    fun getOrDefault(id: String?, default: Teamspace = PersonalTeamspace): Teamspace = id?.let { get(it) } ?: default

    fun canChangeTeamspace(): Boolean

    fun isFeatureEnabled(@Teamspace.Feature featureCheck: String): Boolean

    fun getFeatureValue(@Teamspace.Feature featureCheck: String): String?

    fun startFeatureOrNotify(
        activity: FragmentActivity?,
        @Teamspace.Feature feature: String?,
        featureCall: FeatureCall? = null
    )

    interface FeatureCall {
        fun startFeature()
    }
}

val TeamspaceAccessor.isSsoUser: Boolean
    get() = all.any { it.status == Teamspace.Status.ACCEPTED && it.isSsoUser }

fun TeamspaceAccessor?.is2FAEnforced(): Boolean {
    val enforcementType = this?.getFeatureValue(Teamspace.Feature.ENFORCED_2FA)
    val enforcementDisabled = enforcementType == null || enforcementType == ENFORCEMENT_DISABLED
    return !enforcementDisabled
}
