package com.dashlane.ui.menu;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.hermes.LogRepository;
import com.dashlane.hermes.generated.definitions.Space;
import com.dashlane.hermes.generated.events.user.SelectSpace;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.manager.TeamspaceManager;
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper;
import com.dashlane.util.userfeatures.UserFeaturesChecker;
import com.dashlane.util.userfeatures.UserFeaturesCheckerUtilsKt;
import com.skocken.presentation.provider.BaseDataProvider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;



public class MenuDataProvider extends BaseDataProvider<MenuDef.IPresenter>
        implements MenuDef.IDataProvider, TeamspaceManager.Listener {

    private final MenuUsageLogger mMenuUsageLogger;
    private final UserFeaturesChecker mUserFeature;
    private final ChecklistHelper mChecklistHelper;
    private final LogRepository mLogRepository;

    MenuDataProvider() {
        this(new MenuUsageLogger(SingletonProvider.getSessionManager(),
                                 SingletonProvider.getComponent().getBySessionUsageLogRepository()),
             SingletonProvider.getUserFeatureChecker(),
             new ChecklistHelper(SingletonProvider.getComponent().getUserPreferencesManager()),
             SingletonProvider.getComponent().getLogRepository());
    }

    @VisibleForTesting
    MenuDataProvider(MenuUsageLogger menuUsageLogger,
            UserFeaturesChecker userFeature,
            ChecklistHelper checklistHelper,
            LogRepository logRepository) {
        mMenuUsageLogger = menuUsageLogger;
        mUserFeature = userFeature;
        mChecklistHelper = checklistHelper;
        mLogRepository = logRepository;
        listenTeamspaceManager();
    }

    @Override
    public List<Teamspace> getTeamspaces() {
        TeamspaceManager teamspaceManager = getTeamspaceManagerForSession();
        if (teamspaceManager == null) {
            return new ArrayList<>();
        }
        List<Teamspace> all = new ArrayList<>(teamspaceManager.getAll());
        all.remove(teamspaceManager.getCurrent());
        return all;
    }

    @Override
    public void onTeamspaceSelected(Teamspace teamspace) {
        TeamspaceManager teamspaceManager = getTeamspaceManagerForSession();
        if (teamspaceManager == null) {
            return;
        }
        if (!teamspace.equals(teamspaceManager.getCurrent())) {
            mMenuUsageLogger.logSpaceChange(teamspace);
            mLogRepository.queueEvent(new SelectSpace(toSpace(teamspace)));
        }
        teamspaceManager.setCurrent(teamspace);
    }

    @Override
    public boolean isVPNVisible() {
        return UserFeaturesCheckerUtilsKt.canShowVpn(mUserFeature);
    }

    @Override
    public MenuUsageLogger getMenuUsageLogger() {
        return mMenuUsageLogger;
    }

    @Override
    public boolean isPersonalPlanVisible() {
        return mChecklistHelper.shouldDisplayChecklist();
    }

    @Override
    public void onStatusChanged(Teamspace teamspace, String previousStatus, String newStatus) {
        
    }

    @Override
    public void onChange(Teamspace teamspace) {
        refreshMenu();
    }

    @Override
    public void onTeamspacesUpdate() {
        refreshMenu();
    }

    @VisibleForTesting
    TeamspaceManager getTeamspaceManagerForSession() {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            return SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
        }
        return null;
    }

    private void refreshMenu() {
        getPresenter().refreshMenuList();
    }

    private void listenTeamspaceManager() {
        TeamspaceManager teamspaceManager = getTeamspaceManagerForSession();
        if (teamspaceManager == null) {
            return;
        }
        TeamspaceManagerWeakListener listener = new TeamspaceManagerWeakListener(this);
        listener.listen(teamspaceManager);
    }

    private static Space toSpace(@NonNull Teamspace teamspace) {
        switch (teamspace.getType()) {
            case Teamspace.Type.PERSONAL:
                return Space.PERSONAL;
            case Teamspace.Type.COMBINED:
                return Space.ALL;
            case Teamspace.Type.COMPANY:
                return Space.PROFESSIONAL;
            default:
                throw new IllegalStateException("Unhandled Teamspace type " + teamspace.getType());
        }
    }
}
