package com.dashlane.ui.menu.header;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.ui.menu.MenuDef;
import com.skocken.presentation.viewholder.PresenterViewHolder;

import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.dashlane.ui.menu.header.MenuHeaderDef.IPresenter;
import static com.dashlane.ui.menu.header.MenuHeaderDef.IView;



public class MenuHeaderViewHolder extends PresenterViewHolder<MenuDef.Item, IPresenter>
        implements IView {

    public MenuHeaderViewHolder(View itemView) {
        super(itemView);

        ViewCompat.setOnApplyWindowInsetsListener(itemView, new AutoAdjustPaddingTopListener());

        View upgradeView = findViewByIdEfficient(R.id.user_upgrade_plan);
        View statusWrapperView = findViewByIdEfficient(R.id.menu_user_profile_status_wrapper);

        statusWrapperView.setOnClickListener(
                v -> {
                    if (findViewByIdEfficient(R.id.user_teamspace_status).getVisibility() == View.VISIBLE) {
                        getPresenter().onHeaderTeamspaceSelectorClick();
                    } else {
                        getPresenter().onHeaderProfileClick();
                    }
                });

        upgradeView.setOnClickListener(
                v -> getPresenter().onHeaderUpgradeClick());
    }

    @Override
    public void setStatus(int textResId) {
        TextView textView = findViewByIdEfficient(R.id.user_profile_status);
        textView.setText(textResId);
    }

    @Override
    public void setUsername(String username) {
        TextView textView = findViewByIdEfficient(R.id.user_profile_email);
        textView.setText(username);
    }

    @Override
    public void setIcon(Drawable defaultIcon) {
        ImageView imageView = findViewByIdEfficient(R.id.user_profile_icon);
        imageView.setImageDrawable(defaultIcon);
    }

    @Override
    public void setTeamspaceSelectorVisible(boolean teamspaceVisible) {
        findViewByIdEfficient(R.id.user_teamspace_status).setVisibility(teamspaceVisible ? View.VISIBLE : View.GONE);
        findViewByIdEfficient(R.id.user_teamspace_subtext).setVisibility(teamspaceVisible ? View.VISIBLE : View.GONE);
        findViewByIdEfficient(R.id.user_profile_email).setVisibility(teamspaceVisible ? View.GONE : View.VISIBLE);
        findViewByIdEfficient(R.id.user_profile_status).setVisibility(teamspaceVisible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTeamspaceName(String teamspaceName) {
        TextView textView = findViewByIdEfficient(R.id.user_teamspace_status);
        textView.setText(teamspaceName);
    }

    @Override
    public void setUpgradeVisible(boolean visible) {
        findViewByIdEfficient(R.id.user_upgrade_plan).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelectorIconUp(boolean modeUp) {
        TextView textView = findViewByIdEfficient(R.id.user_teamspace_status);
        int resId = modeUp ? R.drawable.ic_arrow_drop_up : R.drawable.ic_arrow_drop_down;
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0);

        TextView subtextView = findViewByIdEfficient(R.id.user_teamspace_subtext);
        subtextView.setText(modeUp ? R.string.menu_v3_teamspace_select : R.string.menu_v3_teamspace_change);
    }

    @Override
    protected Class<? extends IPresenter> getPresenterClass() {
        return MenuHeaderPresenter.class;
    }

    

    private static class AutoAdjustPaddingTopListener implements OnApplyWindowInsetsListener {

        @Override
        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            int paddingTop = insets.getSystemWindowInsetTop();
            if (v.getPaddingTop() != paddingTop) {
                v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), v.getPaddingBottom());
            }
            return insets.consumeSystemWindowInsets();
        }
    }
}