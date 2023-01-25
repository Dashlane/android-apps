package com.dashlane.ui.menu.teamspace;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.viewholder.PresenterViewHolder;

import static com.dashlane.ui.menu.teamspace.MenuTeamspaceDef.IPresenter;
import static com.dashlane.ui.menu.teamspace.MenuTeamspaceDef.IView;



public class MenuTeamspaceViewHolder extends PresenterViewHolder<MenuDef.Item, IPresenter>
        implements IView {

    public MenuTeamspaceViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setIcon(Drawable icon) {
        ImageView imageView = findViewByIdEfficient(R.id.menu_item_icon);
        imageView.setImageDrawable(icon);
    }

    @Override
    public void setTitle(String teamName) {
        TextView textView = findViewByIdEfficient(R.id.menu_item_title);
        textView.setText(teamName);
    }

    @Override
    protected Class<? extends IPresenter> getPresenterClass() {
        return MenuTeamspacePresenter.class;
    }
}