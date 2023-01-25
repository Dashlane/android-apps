package com.dashlane.ui.menu.item;

import android.graphics.drawable.Drawable;

import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.definition.Base;

import androidx.annotation.Nullable;



public interface MenuItemDef {

    interface IPresenter extends Base.IItemPresenter<MenuDef.Item> {
        void update();
    }

    interface IDataProvider extends Base.IDataProvider {

        boolean isSelected();

        MenuItem getItem();

        void setItem(MenuItem object);
    }

    interface IView extends Base.IItemView<MenuDef.Item> {
        void setIcon(int icoResId);

        void setTitle(int textResId);

        void setSubtitle(@Nullable String subtitle);

        void setUpgradeVisible(boolean visible);

        void setEndIcon(@Nullable Drawable endIcon, @Nullable String description);

        void setSelected(boolean selected);
    }
}
