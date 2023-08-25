package com.dashlane.ui.menu.item;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.design.component.compat.view.BadgeView;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.definition.Base;
import com.skocken.presentation.viewholder.PresenterViewHolder;

import androidx.annotation.Nullable;

public class MenuItemViewHolder extends PresenterViewHolder<MenuDef.Item, MenuItemDef.IPresenter>
        implements MenuItemDef.IView {

    public MenuItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setTitle(int textResId) {
        TextView textView = findViewByIdEfficient(R.id.menu_item_title);
        textView.setText(textResId);
    }

    @Override
    public void setIcon(int icoResId) {
        ImageView imageView = findViewByIdEfficient(R.id.menu_item_icon);
        imageView.setImageResource(icoResId);
    }

    @Override
    public void setSubtitle(@Nullable String subtitle) {
        TextView textView = findViewByIdEfficient(R.id.menu_item_subtitle);
        if (textView == null) return;
        textView.setText(subtitle);
        textView.setVisibility(subtitle != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setEndIcon(@Nullable Drawable endIcon, @Nullable String description) {
        ImageView imageView = findViewByIdEfficient(R.id.menu_item_end_icon);
        if (imageView == null) return;
        imageView.setImageDrawable(endIcon);
        imageView.setVisibility(endIcon != null ? View.VISIBLE : View.GONE);
        if (description != null) {
            imageView.setContentDescription(description);
        }
    }

    @Override
    public void setUpgradeVisible(boolean visible) {
        BadgeView badgeView = findViewByIdEfficient(R.id.menu_item_upgrade);
        if (badgeView == null) return;
        badgeView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelected(boolean selected) {
        getView().setSelected(selected);
    }

    @Override
    protected Class<? extends MenuItemDef.IPresenter> getPresenterClass() {
        return MenuItemPresenter.class;
    }

    @Override
    public void setPresenter(Base.IPresenter presenter) {
        super.setPresenter(presenter);
        if (presenter instanceof MenuItemPresenter) {
            ((MenuItemPresenter) presenter).setProvider(new MenuItemDataProvider());
        }
    }
}