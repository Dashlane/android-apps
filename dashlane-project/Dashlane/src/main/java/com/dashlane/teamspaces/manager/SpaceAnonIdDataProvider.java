package com.dashlane.teamspaces.manager;

import com.dashlane.core.HashForLog;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.Session;
import com.dashlane.settings.SettingsManager;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.useractivity.log.usage.UsageLogCode105;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.util.GsonJsonSerialization;
import com.dashlane.util.JsonSerialization;
import com.dashlane.xml.domain.SyncObject;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.collection.ArrayMap;
import kotlin.Unit;

import static kotlin.text.StringsKt.trim;




public class SpaceAnonIdDataProvider {
    private static final ArrayMap EMPTY_ANON_ID_MAP = new ArrayMap();
    private final SpaceAnonIdLogger mUsageLogger;
    private final JsonSerialization mJsonSerialization;

    public SpaceAnonIdDataProvider(@NonNull BySessionRepository<UsageLogRepository> bySessionUsageLogRepository,
            @NonNull Session session) {
        this(new SpaceAnonIdLogger(bySessionUsageLogRepository, session),
             new GsonJsonSerialization(new Gson())
        );
    }

    @VisibleForTesting
    SpaceAnonIdDataProvider(SpaceAnonIdLogger usageLogger, JsonSerialization jsonSerialization) {
        mUsageLogger = usageLogger;
        mJsonSerialization = jsonSerialization;
    }

    ArrayMap<Teamspace, String> getAnonSpaceIds(SettingsManager settingsManager,
                                                List<Teamspace> joinedTeamspaces) {
        
        ArrayMap deserializedMap = getSettingsSpaceAnonIdMap(settingsManager);

        
        ArrayMap<Teamspace, String> anonymizedIds = new ArrayMap<>(deserializedMap.size());
        anonymizedIds.put(TeamspaceManager.PERSONAL_TEAMSPACE, Teamspace.DEFAULT_SPACE_ANON_ID);
        anonymizedIds.put(TeamspaceManager.COMBINED_TEAMSPACE, Teamspace.ALL_SPACE_ANON_ID);

        boolean hasNewSpaces = false;

        for (int i = 0; i < joinedTeamspaces.size(); i++) {
            Teamspace space = joinedTeamspaces.get(i);
            
            if (!Teamspace.SENTINEL_DEFAULT_ANON_ID.equals(space.getAnonTeamId())) {
                continue;
            }
            String teamId = space.getTeamId();
            String anonId = null;
            if (deserializedMap.containsKey(teamId)) {
                anonId = ((String) deserializedMap.get(teamId));
                anonymizedIds.put(space, anonId);
            } else if (!isDefaultSpace(space) && !isCombinedSpace(space)) {
                hasNewSpaces = true;
                anonId = generatedAnonymizedId(settingsManager, teamId);
            }
            if (anonId != null) {
                
                anonId = trim(anonId, '{', '}');
                anonymizedIds.put(space, anonId);
            }
        }

        if (hasNewSpaces) {
            reportSpaces(settingsManager, anonymizedIds);
        }
        return anonymizedIds;

    }

    void reportSpaces(SettingsManager settingsManager, ArrayMap<Teamspace, String> anonIdMap) {

        
        mUsageLogger.logSpace(UsageLogCode105.Type.PERSONAL, Teamspace.DEFAULT_SPACE_ANON_ID);
        mUsageLogger.logSpace(UsageLogCode105.Type.GLOBAL, Teamspace.ALL_SPACE_ANON_ID);

        ArrayMap<String, String> jsonMap = new ArrayMap<>();
        for (Map.Entry<Teamspace, String> entry : anonIdMap.entrySet()) {
            Teamspace team = entry.getKey();
            if (!isCombinedSpace(team) && !isDefaultSpace(team)) {
                String anonId = entry.getValue();
                mUsageLogger.logSpace(UsageLogCode105.Type.TEAM_OFFER, anonId);
                jsonMap.put(team.getTeamId(), anonId);
            }
        }

        String json = mJsonSerialization.toJson(jsonMap);
        SyncObject.Settings settings = settingsManager.getSettings();
        settingsManager.updateSettings(settings.copy(builder -> {
            builder.setSpaceAnonIds(json);
            return Unit.INSTANCE;
        }), true);
    }

    @VisibleForTesting
    String generatedAnonymizedId(SettingsManager settingsManager, String teamId) {
        String usagelogToken = settingsManager.getSettings().getUsagelogToken();
        return serializeSpaceId(HashForLog.getHashForLog(usagelogToken == null ? "" : usagelogToken, teamId));
    }

    @VisibleForTesting
    String serializeSpaceId(String anonSpaceId) {
        return String.format(Locale.US, "{%s}", anonSpaceId);
    }

    private ArrayMap getSettingsSpaceAnonIdMap(SettingsManager settingsManager) {
        String jsonSpaceAnonIds = settingsManager.getSettings().getSpaceAnonIds();
        ArrayMap result = null;
        if (jsonSpaceAnonIds != null) {
            try {
                result = mJsonSerialization.fromJson(jsonSpaceAnonIds, ArrayMap.class);
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            return EMPTY_ANON_ID_MAP;
        } else {
            return result;
        }
    }

    private boolean isCombinedSpace(Teamspace itemSelected) {
        return itemSelected == TeamspaceManager.COMBINED_TEAMSPACE;
    }

    private boolean isDefaultSpace(Teamspace itemSelected) {
        return itemSelected == TeamspaceManager.PERSONAL_TEAMSPACE;
    }
}
