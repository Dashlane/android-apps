package com.dashlane.ui.menu.teamspace;

import android.graphics.drawable.Drawable;

import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.definition.Base;



public interface MenuTeamspaceDef {

    interface IPresenter extends Base.IItemPresenter<MenuDef.Item> {
    }

    interface IDataProvider extends Base.IDataProvider {
    }

    interface IView extends Base.IItemView<MenuDef.Item> {
        void setIcon(Drawable icon);

        void setTitle(String teamName);
    }
}
