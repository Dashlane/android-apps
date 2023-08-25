package com.dashlane.ui.menu.teamspace;

import static com.dashlane.ui.menu.teamspace.MenuTeamspaceDef.IDataProvider;
import static com.dashlane.ui.menu.teamspace.MenuTeamspaceDef.IPresenter;
import static com.dashlane.ui.menu.teamspace.MenuTeamspaceDef.IView;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.VisibleForTesting;

import com.dashlane.teamspaces.manager.TeamspaceDrawableProvider;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.presenter.BaseItemPresenter;

public class MenuTeamspacePresenter extends BaseItemPresenter<IDataProvider, IView>
        implements IPresenter {

    @Override
    public void updateView(Context context, MenuDef.Item object) {
        if (!(object instanceof TeamspaceAdapterItem)) {
            return;
        }
        Teamspace teamspace = ((TeamspaceAdapterItem) object).getTeamspace();

        IView view = getView();
        view.setTitle(teamspace.getTeamName());
        view.setIcon(getTeamSpaceDrawable(teamspace));
    }

    @VisibleForTesting
    Drawable getTeamSpaceDrawable(Teamspace teamspace) {
        return TeamspaceDrawableProvider.getIcon(getContext(), teamspace);
    }

}