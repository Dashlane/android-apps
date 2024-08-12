package com.dashlane.ui.util;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dashlane.ui.R;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.ui.drawable.BadgeDrawerArrowDrawable;
import com.dashlane.util.ColorUtilsKt;
import com.dashlane.util.ContextUtilsKt;
import com.dashlane.util.StatusBarUtils;


public class ActionBarUtil {

    private DashlaneActivity mActivity;

    private final Toolbar mToolbar;

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private boolean mSetupDone = false;
    private int mLatestColorSet;

    public ActionBarUtil(DashlaneActivity activity) {
        mActivity = activity;
        mToolbar = mActivity.findViewById(R.id.toolbar);
        mLatestColorSet = activity.getColor(R.color.container_agnostic_neutral_standard);
    }

    public void setup() {
        if (mSetupDone) {
            return;
        }
        setToolbarAppearance(getDrawerLayout() != null);
        restoreDefaultActionBarColor();
        setToolbarToActivity();
        mSetupDone = true;
    }

    private void setToolbarAppearance(boolean withUpButton) {
        if (withUpButton) {
            mToolbar.setNavigationIcon(ContextUtilsKt.getThemeAttrDrawable(mActivity, R.attr.homeAsUpIndicator));
        }
        setToolbarToActivity(); 
    }

    public void setDrawerToggle(ActionBarDrawerToggle drawerToggle) {
        mActionBarDrawerToggle = drawerToggle;
        setBadgeDrawerColor(mLatestColorSet);
    }

    public void restoreDefaultActionBarColor() {
        setActionBarColor(mActivity.getColor(R.color.container_agnostic_neutral_standard));
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    @Nullable
    public BadgeDrawerArrowDrawable getDrawerArrowDrawable() {
        if (mActionBarDrawerToggle == null) {
            return null;
        }
        DrawerArrowDrawable drawerArrowDrawable = mActionBarDrawerToggle.getDrawerArrowDrawable();
        BadgeDrawerArrowDrawable badgeDrawable;
        if (drawerArrowDrawable instanceof BadgeDrawerArrowDrawable) {
            return (BadgeDrawerArrowDrawable) drawerArrowDrawable;
        } else {
            badgeDrawable = new BadgeDrawerArrowDrawable(mActivity.getSupportActionBar().getThemedContext());
            mActionBarDrawerToggle.setDrawerArrowDrawable(badgeDrawable);
            return badgeDrawable;
        }
    }

    public void setActionBarColor(int color) {
        mLatestColorSet = color;
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(color);
            ColorUtilsKt.setToolbarContentTint(mToolbar,
                    ColorUtilsKt.getColorOnForToolbar(mToolbar.getContext(), color));
        }
        setBadgeDrawerColor(color);
        StatusBarUtils.setStatusBarColor(mActivity, StatusBarUtils.computeStatusBarColor(color), getDrawerLayout());
    }

    public void setLogo(Drawable logoWrapped) {
        mToolbar.setLogo(logoWrapped);
    }

    public void setLogo(int icoResId) {
        mToolbar.setLogo(icoResId);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    private void setBadgeDrawerColor(int color) {
        BadgeDrawerArrowDrawable badgeDrawerArrowDrawable = getDrawerArrowDrawable();
        if (badgeDrawerArrowDrawable != null) {
            badgeDrawerArrowDrawable.setBackgroundColor(color);
        }
    }

    @SuppressLint("RestrictedApi")
    private void setToolbarToActivity() {
        if (!(mActivity.getSupportActionBar() instanceof WindowDecorActionBar)) {
            
            mActivity.setSupportActionBar(mToolbar);
        }
    }

    private DrawerLayout getDrawerLayout() {
        if (mActivity instanceof DrawerLayoutProvider) {
            return ((DrawerLayoutProvider) mActivity).getNavigationDrawer();
        } else {
            return null;
        }
    }

    public interface DrawerLayoutProvider {

        DrawerLayout getNavigationDrawer();
    }
}
