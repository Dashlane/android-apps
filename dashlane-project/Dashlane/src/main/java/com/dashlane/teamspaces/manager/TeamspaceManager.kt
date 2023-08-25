package com.dashlane.teamspaces.manager

import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.settings.SettingsManager
import com.dashlane.teamspaces.CombinedTeamspace
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.teamspaces.db.TeamspaceUsageLogSpaceChanged
import com.dashlane.teamspaces.manager.TeamspaceAccessor.FeatureCall
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.Constants
import com.dashlane.util.ThreadHelper
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class TeamspaceManager(
    val dataProvider: SpaceAnonIdDataProvider,
    private val settingsManager: SettingsManager,
    teamspaceUsageLogSpaceChanged: TeamspaceUsageLogSpaceChanged?
) : TeamspaceAccessor {
    private val defaultTeamspace = CombinedTeamspace

    private val joinedTeamspaces: MutableList<Teamspace> = ArrayList()
    private val _revokedAndDeclinedSpaces: MutableList<Teamspace> = ArrayList()
    private val listeners: MutableSet<Listener> = HashSet()

    override var current: Teamspace? = defaultTeamspace
        set(teamspace) {
            if (Teamspace.Status.ACCEPTED != teamspace?.status) {
                return
            }
            field = teamspace
            setAsDefault(teamspace)
            notifyAllChangeListeners()
        }

    private val teamspaceNotifier = TeamspaceRestrictionNotificator()

    init {
        subscribeListener(RevokedDetector())
        initEmbeddedTeamspace()
        subscribeListener(teamspaceUsageLogSpaceChanged)
    }

    fun init(data: List<Teamspace>?, forceCategorizationManager: TeamspaceForceCategorizationManager) {
        val previousJoinedTeamspaces = all
        val previousRevokedAndDeclineTeamspaces: List<Teamspace> = ArrayList(_revokedAndDeclinedSpaces)
        joinedTeamspaces.clear()
        _revokedAndDeclinedSpaces.clear()

        
        joinedTeamspaces.add(0, CombinedTeamspace)
        joinedTeamspaces.add(1, PersonalTeamspace)
        var i = 0
        while (data != null && i < data.size) {
            val t = data[i]
            val status = t.status
            if (status == null) {
                i++
                continue
            }
            when (status) {
                Teamspace.Status.DECLINED, Teamspace.Status.REVOKED -> _revokedAndDeclinedSpaces.add(t)
                Teamspace.Status.ACCEPTED -> joinedTeamspaces.add(t)
            }
            i++
        }
        val dataProvider = dataProvider
        setupAnalyticsIds(dataProvider)
        reloadCurrentSpace()

        
        notifyAllUpdateListeners()
        notifyChangeStatus(previousJoinedTeamspaces, previousRevokedAndDeclineTeamspaces)
        forceCategorizationManager.executeAsync()
    }

    override val all: List<Teamspace>
        get() = joinedTeamspaces.filterNotNull().toList()

    override val revokedAndDeclinedSpaces: List<Teamspace>
        get() = _revokedAndDeclinedSpaces.filterNotNull().toList()

    override operator fun get(id: String): Teamspace? {
        val teamspace = getTeamspace(all, id)
        return teamspace ?: getTeamspace(_revokedAndDeclinedSpaces, id)
    }

    override fun isCurrent(id: String): Boolean {
        return !canChangeTeamspace() || current != null && current!!.teamId == id
    }

    val isSpaceSelected: Boolean
        get() = CombinedTeamspace != current

    override fun canChangeTeamspace(): Boolean {
        return all.size > 2
    }

    fun subscribeListener(listener: Listener?) {
        listener?.let {
            listeners.add(it)
        }
    }

    fun unSubscribeListeners(listener: Listener?) {
        listeners.remove(listener)
    }

    @VisibleForTesting
    fun setupAnalyticsIds(dataProvider: SpaceAnonIdDataProvider, settingsManager: SettingsManager?) {
        val teamspaces: MutableList<Teamspace> = ArrayList()
        teamspaces.addAll(all)
        teamspaces.addAll(_revokedAndDeclinedSpaces)
        val anonSpaceMap = dataProvider.getAnonSpaceIds(settingsManager, teamspaces)
        for (i in teamspaces.indices) {
            val space = teamspaces[i]
            val anonId = anonSpaceMap[space]
            space.anonTeamId = anonId
        }
    }

    private fun setupAnalyticsIds(dataProvider: SpaceAnonIdDataProvider) {
        setupAnalyticsIds(dataProvider, settingsManager)
    }

    private fun notifyAllUpdateListeners() {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread { notifyAllUpdateListeners() }
            return
        }
        val listeners: Set<Listener> = HashSet(listeners)
        for (reference in listeners) {
            reference.onTeamspacesUpdate()
        }
    }

    private fun notifyAllChangeListeners() {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread { notifyAllChangeListeners() }
            return
        }
        val listeners: Set<Listener> = HashSet(listeners)
        for (reference in listeners) {
            reference.onChange(current)
        }
    }

    private fun setAsDefault(teamspace: Teamspace) {
        try {
            val jsonDescriptor = JSONObject()
            jsonDescriptor.put(Constants.DEFAULT_TEAMSPACE_TYPE, teamspace.type)
            jsonDescriptor.put(Constants.DEFAULT_TEAMSPACE_ID, teamspace.teamId)
            SingletonProvider.getUserPreferencesManager().putString(
                Constants.DEFAULT_TEAMSPACE,
                jsonDescriptor.toString()
            )
        } catch (e: JSONException) {
            warn("failed to store default space, json descriptor failed to create", LOG_TAG, e)
        }
    }

    private fun reloadCurrentSpace() {
        current = defaultSpaceType
    }

    private val defaultSpaceType: Teamspace
        get() {
            val preferencesManager = SingletonProvider.getUserPreferencesManager()
            try {
                if (!preferencesManager.exist(Constants.DEFAULT_TEAMSPACE)) {
                    return defaultTeamspace
                }
                val descriptor = preferencesManager.getString(Constants.DEFAULT_TEAMSPACE)
                val jsonDescriptor = JSONObject(descriptor)
                val type = jsonDescriptor.getInt(Constants.DEFAULT_TEAMSPACE_TYPE)
                val id = jsonDescriptor.optString(Constants.DEFAULT_TEAMSPACE_ID)
                return when (type) {
                    Teamspace.Type.PERSONAL -> PersonalTeamspace
                    Teamspace.Type.COMBINED -> CombinedTeamspace
                    Teamspace.Type.COMPANY -> {
                        for (space in all) {
                            if (id != null && id == space.teamId) {
                                return space
                            }
                        }
                        defaultTeamspace
                    }
                    else -> defaultTeamspace
                }
            } catch (e: JSONException) {
                warn("failed to store default space, json descriptor failed to create", LOG_TAG, e)
            }
            return defaultTeamspace
        }

    private fun initEmbeddedTeamspace() {
        init(PersonalTeamspace, R.string.teamspace_personal, R.color.teamspace_personal)
        init(CombinedTeamspace, R.string.teamspace_combined, R.color.teamspace_combined)
    }

    private fun init(teamspace: Teamspace, @StringRes labelResId: Int, @ColorRes colorResId: Int) {
        val context = SingletonProvider.getContext() ?: return
        val label = context.getString(labelResId)
        val color = ContextCompat.getColor(context, colorResId)
        teamspace.teamName = label
        teamspace.companyName = label
        teamspace.displayLetter = label.substring(0, 1)
        teamspace.color = String.format(Locale.US, "#%06X", 0xFFFFFF and color)
    }

    override fun isFeatureEnabled(@Teamspace.Feature featureCheck: String): Boolean {
        if (!canChangeTeamspace()) {
            
            return true
        }
        if (!featureCheck.isNotSemanticallyNull()) {
            
            return true
        }
        for (space in all) {
            
            if (space.type == Teamspace.Type.PERSONAL ||
                space.type == Teamspace.Type.COMBINED
            ) {
                
                continue
            }
            if (space.featureDisabledForSpace(featureCheck)) {
                return false
            }
        }
        return true
    }

    override fun getFeatureValue(@Teamspace.Feature featureCheck: String): String? {
        if (!canChangeTeamspace()) {
            
            return null
        }
        if (!featureCheck.isNotSemanticallyNull()) {
            
            return null
        }
        for (space in all) {
            
            if (space.type == Teamspace.Type.PERSONAL ||
                space.type == Teamspace.Type.COMBINED
            ) {
                
                continue
            }
            return space.getFeatureValue(featureCheck)
        }
        return null
    }

    private fun notifyChangeStatus(
        previousJoinedTeamspaces: List<Teamspace>,
        previousRevokedAndDeclineTeamspaces: List<Teamspace>
    ) {
        if (previousJoinedTeamspaces.isEmpty()) {
            
            return
        }
        val allTeamspaces = all

        for (teamspace in allTeamspaces.drop(2)) { 
            val previousStatus: String? = if (isPresent(previousJoinedTeamspaces, teamspace)) {
                continue 
            } else if (isPresent(previousRevokedAndDeclineTeamspaces, teamspace)) {
                Teamspace.Status.REVOKED
            } else {
                null
            }
            sendStatusUpdate(teamspace, previousStatus)
        }
        for (teamspace in _revokedAndDeclinedSpaces) {
            val previousStatus: String? = if (isPresent(previousJoinedTeamspaces, teamspace)) {
                Teamspace.Status.ACCEPTED
            } else {
                val previousSpace = getTeamspace(previousRevokedAndDeclineTeamspaces, teamspace.teamId)
                if (previousSpace == null) {
                    null
                } else if (previousSpace.shouldDeleteForceCategorizedContent() ==
                    teamspace.shouldDeleteForceCategorizedContent()
                ) {
                    continue 
                } else {
                    Teamspace.Status.REVOKED
                }
            }
            sendStatusUpdate(teamspace, previousStatus)
        }
    }

    private fun sendStatusUpdate(teamspace: Teamspace, previousStatus: String?) {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread { sendStatusUpdate(teamspace, previousStatus) }
            return
        }
        val listeners: Set<Listener> = HashSet(listeners)
        for (reference in listeners) {
            reference.onStatusChanged(teamspace, previousStatus, teamspace.status)
        }
    }

    private fun getTeamspace(teamspaces: List<Teamspace>, id: String?) = teamspaces.firstOrNull { it.teamId == id }

    private fun isPresent(list: List<Teamspace>, teamspace: Teamspace): Boolean {
        return getTeamspace(list, teamspace.teamId) != null
    }

    interface Listener {
        @MainThread
        fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?)

        @MainThread
        fun onChange(teamspace: Teamspace?)

        @MainThread
        fun onTeamspacesUpdate()
    }

    override fun startFeatureOrNotify(
        activity: FragmentActivity?,
        @Teamspace.Feature feature: String?,
        featureCall: FeatureCall?
    ) {
        if (Teamspace.Feature.SECURE_NOTES_DISABLED == feature && SingletonProvider.getUserFeatureChecker()
                .has(UserFeaturesChecker.FeatureFlip.DISABLE_SECURE_NOTES)
        ) {
            teamspaceNotifier.notifyFeatureRestricted(activity, feature)
            return
        }
        if (!isFeatureEnabled(feature!!)) {
            teamspaceNotifier.notifyFeatureRestricted(activity, feature)
            return
        }
        featureCall!!.startFeature()
    }

    companion object {
        private const val LOG_TAG = "TEAMSPACE"
    }
}