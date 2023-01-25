package com.dashlane.ui.menu.header;

import android.graphics.drawable.Drawable;

import com.dashlane.premium.offer.common.model.UserBenefitStatus;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.definition.Base;



public interface MenuHeaderDef {

    interface IPresenter extends Base.IItemPresenter<MenuDef.Item> {
        void onHeaderTeamspaceSelectorClick();

        void onHeaderProfileClick();

        void onHeaderUpgradeClick();
    }

    interface IDataProvider extends Base.IDataProvider {
        String getUserAlias();

        Teamspace getCurrentTeamspace();

        UserBenefitStatus.Type getStatusType();
    }

    interface IView extends Base.IItemView<MenuDef.Item> {
        void setStatus(int textResId);

        void setUsername(String username);

        void setIcon(Drawable defaultIcon);

        void setTeamspaceSelectorVisible(boolean visible);

        void setTeamspaceName(String teamspaceName);

        void setSelectorIconUp(boolean modeUp);

        void setUpgradeVisible(boolean visible);
    }
}
