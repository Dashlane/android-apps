package com.dashlane.ui.dialogs.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.dashlane.R;
import com.dashlane.core.premium.PremiumStatus;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.manager.RevokedDetector;
import com.dashlane.teamspaces.manager.TeamspaceDrawableProvider;
import com.dashlane.teamspaces.manager.TeamspaceManager;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.activities.HomeActivity;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;

import java.lang.ref.WeakReference;

@SuppressWarnings("java:S110")
public class SpaceRevokedDialog extends NotificationDialogFragment {

    private static final String ARG_SPACE_REVOKED_ID = "ARG_SPACE_REVOKED_ID";

    private static UpcomingChangeListener sUpcomingChangeListener;

    private long mStartShown;

    public static void listenUpcoming(HomeActivity homeActivity) {
        stopUpcomingListener();
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            return;
        }
        TeamspaceManager teamManager =
                SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
        if (teamManager == null) {
            return;
        }
        sUpcomingChangeListener = new UpcomingChangeListener(homeActivity);
        teamManager.subscribeListener(sUpcomingChangeListener);
    }

    public static void stopUpcomingListener() {
        if (sUpcomingChangeListener == null) {
            return; 
        }
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            return;
        }
        TeamspaceManager teamManager =
                SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
        if (teamManager != null) {
            teamManager.unSubscribeListeners(sUpcomingChangeListener);
        }
        sUpcomingChangeListener = null; 
    }

    public static void showIfNecessary(Context context, FragmentManager fragmentManager) {
        if (context == null) {
            return; 
        }
        TeamspaceManager teamspaceManager;
        PremiumStatus premiumStatus;
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            teamspaceManager =
                    SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
            premiumStatus = SingletonProvider.getComponent().getAccountStatusRepository().getPremiumStatus(session);
        } else {
            return; 
        }
        UserPreferencesManager preferencesManager = SingletonProvider.getUserPreferencesManager();
        String spaceId = preferencesManager.getString(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR);
        if (spaceId == null) {
            return;
        }
        Teamspace teamspace = teamspaceManager.get(spaceId);
        if (teamspace == null
            || !Teamspace.Status.REVOKED.equals(teamspace.getStatus())
            || teamspace.shouldDelete()) {
            preferencesManager.remove(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR);
            return; 
        }

        Bundle args = new Bundle();
        args.putString(ARG_SPACE_REVOKED_ID, spaceId);

        String title = context.getString(R.string.space_revoked_popup_title, teamspace.getTeamName());
        String datePremiumOver = DateUtils.formatDateTime(context, premiumStatus.getExpiryDate().toEpochMilli(),
                                                          DateUtils.FORMAT_SHOW_DATE |
                                                          DateUtils.FORMAT_SHOW_YEAR |
                                                          DateUtils.FORMAT_NO_MONTH_DAY |
                                                          DateUtils.FORMAT_ABBREV_ALL);
        String description = context.getString(R.string.space_revoked_popup_description, datePremiumOver);

        SpaceRevokedDialog dialog = new Builder().setArgs(args)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButtonText(context, R.string.ok)
                .build(new SpaceRevokedDialog());
        dialog.show(fragmentManager, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        addTeamspaceIcon();
    }

    private void addTeamspaceIcon() {
        View topLayout = getDialogTitleContainer();

        if (topLayout instanceof LinearLayout
            
            && ((LinearLayout) topLayout).getOrientation() == LinearLayout.VERTICAL
            
            && ((LinearLayout) topLayout).getChildCount() == 1) {
            
            ((LinearLayout) topLayout).addView(
                    generateTeamspaceIconView(), 0);
        }
    }

    @Nullable
    private View getDialogTitleContainer() {
        try {
            ViewParent parent =
                    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).getParent().getParent();
            if (parent instanceof LinearLayout
                
                && ((LinearLayout) parent).getOrientation() == LinearLayout.VERTICAL
                
                && ((LinearLayout) parent).getChildCount() == 4) {
                return ((LinearLayout) parent).getChildAt(0);
            }
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mStartShown = System.currentTimeMillis();

    }

    @Override
    public void onPause() {
        super.onPause();
        markAsRead();
    }

    private View generateTeamspaceIconView() {
        Teamspace teamspace = null;
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            String spaceId = getArguments().getString(ARG_SPACE_REVOKED_ID);
            if (spaceId != null) {
                TeamspaceManager teamManager =
                        SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
                teamspace = teamManager.get(spaceId);
            }
        }
        Drawable icon =
                TeamspaceDrawableProvider.getIcon(getContext(), teamspace, R.dimen.big_clickable_area_size);
        ImageView view = new ImageView(getContext());
        view.setImageDrawable(icon);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                               ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.spacing_huge);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private void markAsRead() {
        if (mStartShown == 0 || (System.currentTimeMillis() - mStartShown) < 500) {
            
            return;
        }
        UserPreferencesManager preferencesManager = SingletonProvider.getUserPreferencesManager();
        String spaceId = preferencesManager.getString(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR);
        if (spaceId != null && spaceId.equals(getArguments().getString(ARG_SPACE_REVOKED_ID))) {
            
            preferencesManager.remove(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR);
        }
    }

    private static class UpcomingChangeListener implements TeamspaceManager.Listener {

        private final WeakReference<HomeActivity> mHomeActivityRef;

        private UpcomingChangeListener(HomeActivity homeActivity) {
            mHomeActivityRef = new WeakReference<>(homeActivity);
        }

        @Override
        public void onStatusChanged(Teamspace teamspace, String previousStatus, String newStatus) {
            if (!RevokedDetector.isSpaceJustRevoked(teamspace, previousStatus, newStatus)) {
                return;
            }
            
            SingletonProvider.getThreadHelper().postDelayed(() -> {
                HomeActivity homeActivity = mHomeActivityRef.get();
                if (homeActivity == null || !homeActivity.isResume()) {
                    return;
                }
                FragmentManager fragmentManager = homeActivity.getSupportFragmentManager();
                showIfNecessary(homeActivity, fragmentManager);
            }, 500);
        }

        @Override
        public void onChange(Teamspace teamspace) {
            
        }

        @Override
        public void onTeamspacesUpdate() {
            
        }
    }
}
