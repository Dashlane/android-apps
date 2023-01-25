package com.dashlane.teamspaces.manager;

import android.content.Context;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.logger.Log;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.settings.SettingsManager;
import com.dashlane.teamspaces.CombinedTeamspace;
import com.dashlane.teamspaces.PersonalTeamspace;
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager;
import com.dashlane.teamspaces.db.TeamspaceUsageLogSpaceChanged;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.util.Constants;
import com.dashlane.util.StringUtils;
import com.dashlane.util.ThreadHelper;
import com.dashlane.util.userfeatures.UserFeaturesChecker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.ColorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.collection.ArrayMap;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;



public class TeamspaceManager implements TeamspaceAccessor {

    private static final String LOG_TAG = "TEAMSPACE";

    public static final Teamspace PERSONAL_TEAMSPACE = PersonalTeamspace.INSTANCE;
    public static final Teamspace COMBINED_TEAMSPACE = CombinedTeamspace.INSTANCE;
    private static final Teamspace DEFAULT_TEAMSPACE = COMBINED_TEAMSPACE;

    private final SpaceAnonIdDataProvider mDataProvider;

    private final List<Teamspace> mJoinedTeamspaces = new ArrayList<>();
    private final List<Teamspace> mRevokedAndDeclinedSpaces = new ArrayList<>();
    private final Set<Listener> mListeners = new HashSet<>();

    private Teamspace mCurrent = DEFAULT_TEAMSPACE;
    private TeamspaceRestrictionNotificator mTeamspaceNotifier;
    private SettingsManager mSettingsManager;

    public TeamspaceManager(SpaceAnonIdDataProvider spaceAnonIdDataProvider,
                            SettingsManager settingsManager,
                            List<Teamspace> teamspaces,
                            TeamspaceUsageLogSpaceChanged teamspaceUsageLogSpaceChanged) {
        subscribeListener(new RevokedDetector());
        mSettingsManager = settingsManager;
        mDataProvider = spaceAnonIdDataProvider;
        initEmbeddedTeamspace();
        mTeamspaceNotifier = new TeamspaceRestrictionNotificator();
        subscribeListener(teamspaceUsageLogSpaceChanged);
    }

    public final void init(List<Teamspace> data, TeamspaceForceCategorizationManager forceCategorizationManager) {

        List<Teamspace> previousJoinedTeamspaces = getAll();
        List<Teamspace> previousRevokedAndDeclineTeamspaces = new ArrayList<>(mRevokedAndDeclinedSpaces);

        mJoinedTeamspaces.clear();
        mRevokedAndDeclinedSpaces.clear();

        
        mJoinedTeamspaces.add(0, COMBINED_TEAMSPACE);
        mJoinedTeamspaces.add(1, PERSONAL_TEAMSPACE);

        for (int i = 0; data != null && i < data.size(); i++) {
            Teamspace t = data.get(i);
            String status = t.getStatus();
            if (status == null) {
                continue;
            }
            switch (status) {
                case Teamspace.Status.DECLINED:
                case Teamspace.Status.REVOKED:
                    mRevokedAndDeclinedSpaces.add(t);
                    break;
                case Teamspace.Status.ACCEPTED:
                    mJoinedTeamspaces.add(t);
                    break;
            }
        }

        SpaceAnonIdDataProvider dataProvider = getDataProvider();
        setupAnalyticsIds(dataProvider);

        reloadCurrentSpace();

        

        notifyAllUpdateListeners();

        notifyChangeStatus(previousJoinedTeamspaces, previousRevokedAndDeclineTeamspaces);

        forceCategorizationManager.executeAsync();
    }

    

    public SpaceAnonIdDataProvider getDataProvider() {
        return mDataProvider;
    }

    

    @NonNull
    @Override
    public List<Teamspace> getAll() {
        List<Teamspace> allJoinedTeamspaces = new ArrayList<>(mJoinedTeamspaces);
        List<Teamspace> nonNullTeamspaces = new ArrayList<>(allJoinedTeamspaces.size());
        for (Teamspace teamspace : allJoinedTeamspaces) {
            if (teamspace != null) {
                nonNullTeamspaces.add(teamspace);
            }
        }
        return nonNullTeamspaces;
    }

    

    @NonNull
    @Override
    public List<Teamspace> getRevokedAndDeclinedSpaces() {
        return mRevokedAndDeclinedSpaces;
    }

    @Override
    public Teamspace get(@NonNull String id) {
        Teamspace teamspace = getTeamspace(getAll(), id);
        if (teamspace != null) return teamspace;
        return getTeamspace(mRevokedAndDeclinedSpaces, id);
    }

    @Override
    public boolean isCurrent(@NonNull String id) {
        return !canChangeTeamspace() 
               || (mCurrent != null && Objects.equals(mCurrent.getTeamId(), id));
    }

    

    @Override
    public Teamspace getCurrent() {
        return mCurrent;
    }

    @NotNull
    @Override
    public Teamspace getCombinedTeamspace() {
        return COMBINED_TEAMSPACE;
    }

    

    @Override
    public void setCurrent(Teamspace teamspace) {
        if (!Teamspace.Status.ACCEPTED.equals(teamspace.getStatus())) {
            return;
        }
        mCurrent = teamspace;
        setAsDefault(teamspace);

        notifyAllChangeListeners();
    }

    

    public boolean isSpaceSelected() {
        return !COMBINED_TEAMSPACE.equals(getCurrent());
    }

    

    @Override
    public boolean canChangeTeamspace() {
        return getAll().size() > 2;
    }

    

    public final void subscribeListener(Listener listener) {
        mListeners.add(listener);
    }

    

    public void unSubscribeListeners(@Nullable Listener listener) {
        mListeners.remove(listener);
    }

    @VisibleForTesting
    void setupAnalyticsIds(SpaceAnonIdDataProvider dataProvider, SettingsManager settingsManager) {
        List<Teamspace> teamspaces = new ArrayList<>();
        teamspaces.addAll(getAll());
        teamspaces.addAll(mRevokedAndDeclinedSpaces);
        ArrayMap<Teamspace, String> anonSpaceMap = dataProvider.getAnonSpaceIds(settingsManager, teamspaces);
        for (int i = 0; i < teamspaces.size(); i++) {
            Teamspace space = teamspaces.get(i);
            String anonId = anonSpaceMap.get(space);
            space.setAnonTeamId(anonId);
        }
    }

    private void setupAnalyticsIds(SpaceAnonIdDataProvider dataProvider) {
        setupAnalyticsIds(dataProvider, mSettingsManager);
    }

    private void notifyAllUpdateListeners() {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread(() -> notifyAllUpdateListeners());
            return;
        }
        Set<Listener> listeners = new HashSet<>(mListeners);
        for (Listener reference : listeners) {
            reference.onTeamspacesUpdate();
        }
    }

    private void notifyAllChangeListeners() {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread(() -> notifyAllChangeListeners());
            return;
        }
        Set<Listener> listeners = new HashSet<>(mListeners);
        for (Listener reference : listeners) {
            reference.onChange(mCurrent);
        }
    }

    private void setAsDefault(@NonNull Teamspace teamspace) {
        try {
            JSONObject jsonDescriptor = new JSONObject();
            jsonDescriptor.put(Constants.DEFAULT_TEAMSPACE_TYPE, teamspace.getType());
            jsonDescriptor.put(Constants.DEFAULT_TEAMSPACE_ID, teamspace.getTeamId());

            SingletonProvider.getUserPreferencesManager().putString(Constants.DEFAULT_TEAMSPACE,
                                                                    jsonDescriptor.toString());
        } catch (JSONException e) {
            Log.v(LOG_TAG, "failed to store default space, json descriptor failed to create", e);
        }
    }

    

    private void reloadCurrentSpace() {
        mCurrent = getDefaultSpaceType();
    }

    @NonNull
    private Teamspace getDefaultSpaceType() {
        UserPreferencesManager preferencesManager = SingletonProvider.getUserPreferencesManager();
        try {
            if (!preferencesManager.exist(Constants.DEFAULT_TEAMSPACE)) {
                return DEFAULT_TEAMSPACE;
            }
            String descriptor = preferencesManager.getString(Constants.DEFAULT_TEAMSPACE);
            JSONObject jsonDescriptor = new JSONObject(descriptor);
            int type = jsonDescriptor.getInt(Constants.DEFAULT_TEAMSPACE_TYPE);
            String id = jsonDescriptor.optString(Constants.DEFAULT_TEAMSPACE_ID);
            switch (type) {
                case Teamspace.Type.PERSONAL:
                    return PERSONAL_TEAMSPACE;
                case Teamspace.Type.COMBINED:
                    return COMBINED_TEAMSPACE;
                case Teamspace.Type.COMPANY:
                    for (Teamspace space : getAll()) {
                        if (id != null && id.equals(space.getTeamId())) {
                            return space;
                        }
                    }
                    return DEFAULT_TEAMSPACE;
                default:
                    return DEFAULT_TEAMSPACE;
            }
        } catch (JSONException e) {
            Log.v(LOG_TAG, "failed to store default space, json descriptor failed to create", e);
        }
        return DEFAULT_TEAMSPACE;


    }

    private void initEmbeddedTeamspace() {
        init(PERSONAL_TEAMSPACE, R.string.teamspace_personal, R.color.teamspace_personal);
        init(COMBINED_TEAMSPACE, R.string.teamspace_combined, R.color.teamspace_combined);
    }

    private void init(Teamspace teamspace, @StringRes int labelResId, @ColorRes int colorResId) {
        Context context = SingletonProvider.getContext();
        if (context == null) {
            return;
        }
        String label = context.getString(labelResId);
        int color = ContextCompat.getColor(context, colorResId);
        teamspace.setTeamName(label);
        teamspace.setCompanyName(label);
        teamspace.setDisplayLetter(label.substring(0, 1));
        teamspace.setColor(String.format(Locale.US, "#%06X", (0xFFFFFF & color)));
    }

    

    @Override
    public boolean isFeatureEnabled(@Teamspace.Feature String featureCheck) {
        if (!canChangeTeamspace()) {
            
            return true;

        }
        if (!StringUtils.isNotSemanticallyNull(featureCheck)) {
            
            return true;
        }
        for (Teamspace space : getAll()) {
            
            if (space.getType() == Teamspace.Type.PERSONAL ||
                space.getType() == Teamspace.Type.COMBINED) {
                
                continue;
            }
            if (space.featureDisabledForSpace(featureCheck)) {
                return false;
            }
        }
        return true;
    }

    

    @Nullable
    @Override
    public String getFeatureValue(@NonNull @Teamspace.Feature String featureCheck) {
        if (!canChangeTeamspace()) {
            
            return null;

        }
        if (!StringUtils.isNotSemanticallyNull(featureCheck)) {
            
            return null;
        }
        for (Teamspace space : getAll()) {
            
            if (space.getType() == Teamspace.Type.PERSONAL ||
                space.getType() == Teamspace.Type.COMBINED) {
                
                continue;
            }
            return space.getFeatureValue(featureCheck);
        }
        return null;
    }

    private void notifyChangeStatus(List<Teamspace> previousJoinedTeamspaces,
                                    List<Teamspace> previousRevokedAndDeclineTeamspaces) {
        if (previousJoinedTeamspaces.size() == 0) {
            
            return;
        }
        List<Teamspace> allTeamspaces = getAll();
        for (int i = 2; i < allTeamspaces.size(); i++) { 
            Teamspace teamspace = allTeamspaces.get(i);
            String previousStatus;
            if (isPresent(previousJoinedTeamspaces, teamspace)) {
                continue; 
            } else if (isPresent(previousRevokedAndDeclineTeamspaces, teamspace)) {
                previousStatus = Teamspace.Status.REVOKED;
            } else {
                previousStatus = null;
            }
            sendStatusUpdate(teamspace, previousStatus);
        }
        for (int i = 0; i < mRevokedAndDeclinedSpaces.size(); i++) {
            Teamspace teamspace = mRevokedAndDeclinedSpaces.get(i);
            String previousStatus;
            if (isPresent(previousJoinedTeamspaces, teamspace)) {
                previousStatus = Teamspace.Status.ACCEPTED;
            } else {
                Teamspace previousSpace = getTeamspace(previousRevokedAndDeclineTeamspaces, teamspace.getTeamId());
                if (previousSpace == null) {
                    previousStatus = null;
                } else if (previousSpace.shouldDeleteForceCategorizedContent() ==
                           teamspace.shouldDeleteForceCategorizedContent()) {
                    continue; 
                } else {
                    previousStatus = Teamspace.Status.REVOKED;
                }
            }
            sendStatusUpdate(teamspace, previousStatus);
        }
    }

    private void sendStatusUpdate(final Teamspace teamspace, final String previousStatus) {
        if (!ThreadHelper.isMainThread()) {
            SingletonProvider.getThreadHelper().runOnMainThread(() -> sendStatusUpdate(teamspace, previousStatus));
            return;
        }
        Set<Listener> listeners = new HashSet<>(mListeners);
        for (Listener reference : listeners) {
            reference.onStatusChanged(teamspace, previousStatus, teamspace.getStatus());
        }
    }

    @Nullable
    private Teamspace getTeamspace(List<Teamspace> teamspaces, String id) {
        for (int i = 0, size = teamspaces.size(); i < size; i++) {
            Teamspace teamspace = teamspaces.get(i);
            if (Objects.equals(teamspace.getTeamId(), id)) {
                return teamspace;
            }
        }
        return null;
    }

    private boolean isPresent(List<Teamspace> list, Teamspace teamspace) {
        return getTeamspace(list, teamspace.getTeamId()) != null;
    }

    

    public interface Listener {
        

        @MainThread
        void onStatusChanged(Teamspace teamspace, String previousStatus, String newStatus);

        

        @MainThread
        void onChange(Teamspace teamspace);

        

        @MainThread
        void onTeamspacesUpdate();
    }

    @Override
    public void startFeatureOrNotify(FragmentActivity activity,
                                     @Teamspace.Feature String feature,
                                     FeatureCall featureCall) {
        if (Teamspace.Feature.SECURE_NOTES_DISABLED.equals(feature)
            && SingletonProvider.getUserFeatureChecker().has(UserFeaturesChecker.FeatureFlip.DISABLE_SECURE_NOTES)) {
            mTeamspaceNotifier.notifyFeatureRestricted(activity, feature);
            return;
        }

        if (!isFeatureEnabled(feature)) {
            mTeamspaceNotifier.notifyFeatureRestricted(activity, feature);
            return;
        }
        featureCall.startFeature();
    }
}
