package com.dashlane.ui.menu.header;

import static com.dashlane.ui.menu.header.MenuHeaderDef.IDataProvider;
import static com.dashlane.ui.menu.header.MenuHeaderDef.IPresenter;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.premium.offer.common.model.UserBenefitStatus;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.manager.TeamspaceManager;
import com.dashlane.teamspaces.model.Teamspace;
import com.skocken.presentation.provider.BaseDataProvider;



public class MenuHeaderDataProvider extends BaseDataProvider<IPresenter> implements IDataProvider {

    @Override
    public String getUserAlias() {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) return null;
        return session.getUserId();
    }

    @Override
    public Teamspace getCurrentTeamspace() {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            TeamspaceManager teamManager =
                    SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
            if (teamManager != null && teamManager.canChangeTeamspace()) {
                return teamManager.getCurrent();
            }
        }
        return null;
    }

    @Override
    public UserBenefitStatus.Type getStatusType() {
        return SingletonProvider.getComponent().getPremiumStatusManager().getFormattedStatus().getType();
    }
}
