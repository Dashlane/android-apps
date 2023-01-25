package com.dashlane.ui.menu.item;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.dashlane.R;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.presenter.BaseItemPresenter;

import org.jetbrains.annotations.NotNull;

import static com.dashlane.ui.menu.item.MenuItemDef.IDataProvider;
import static com.dashlane.ui.menu.item.MenuItemDef.IPresenter;
import static com.dashlane.ui.menu.item.MenuItemDef.IView;



public class MenuItemPresenter extends BaseItemPresenter<IDataProvider, IView>
        implements IPresenter {

    @Override
    public void updateView(@NotNull Context context, MenuDef.Item object) {
        if (!(object instanceof MenuItem)) {
            return;
        }
        getProvider().setItem((MenuItem) object);
        update();
    }

    @Override
    public void update() {
        MenuItem object = getProvider().getItem();
        IView view = getView();
        if (object == null) {
            return;
        }
        view.setTitle(object.getTitleResId());
        String subtitle = null;
        Drawable endIcon = object.getEndIcon(view.getContext());
        String endIconDescription = object.getEndIconDescription(view.getContext());
        MenuItem.PremiumTag premiumTag = object.getPremiumTag();
        if (premiumTag instanceof MenuItem.PremiumTag.Trial) {
            subtitle = view.getContext().getString(R.string.menu_v3_remaining_days,
                    ((MenuItem.PremiumTag.Trial) premiumTag).getRemainingDays());
        }
        view.setUpgradeVisible(premiumTag instanceof MenuItem.PremiumTag.PremiumOnly);
        view.setSubtitle(subtitle);
        view.setEndIcon(endIcon, endIconDescription);
        IDataProvider provider = getProvider();
        boolean isSelected = provider.isSelected();
        if (isSelected) {
            view.setIcon(object.getIconSelectedResId());
        } else {
            view.setIcon(object.getIconResId());
        }
        view.setSelected(isSelected);
    }
}