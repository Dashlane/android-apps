package com.dashlane.teamspaces.manager;

import android.content.Context;
import android.widget.Toast;

import com.dashlane.R;
import com.dashlane.account.UserAccountInfo;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.navigation.NavigationUtils;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.model.Teamspace;



public class RevokedDetector implements TeamspaceManager.Listener {
    @Override
    public void onStatusChanged(Teamspace teamspace, String previousStatus, String newStatus) {
        if (!isSpaceJustRevoked(teamspace, previousStatus, newStatus)) {
            return;
        }
        
        Context context = SingletonProvider.getContext();
        if (context == null) {
            return; 
        }

        
        if (logoutIfRevokedFromSso(context, teamspace)) {
            return;
        }

        SingletonProvider.getUserPreferencesManager()
                         .putString(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR, teamspace.getTeamId());
    }

    @Override
    public void onChange(Teamspace teamspace) {
        
    }

    @Override
    public void onTeamspacesUpdate() {
        
    }

    public static boolean isSpaceJustRevoked(Teamspace teamspace, String previousStatus, String newStatus) {
        return teamspace != null
               && Teamspace.Status.ACCEPTED.equals(previousStatus)
               && Teamspace.Status.REVOKED.equals(newStatus);
    }

    private static boolean logoutIfRevokedFromSso(Context context, Teamspace teamspace) {
        Session session = SingletonProvider.getSessionManager().getSession();

        if (session == null) {
            return false;
        }


        UserAccountInfo userAccountInfo = SingletonProvider.getComponent()
                                                           .getUserAccountStorage()
                                                           .get(session.getUsername());

        if (userAccountInfo == null || !userAccountInfo.getSso()) {
            
            return false;
        }

        SingletonProvider.getToaster()
                         .show(context.getString(R.string.space_revoked_popup_title, teamspace.getTeamName()),
                               Toast.LENGTH_LONG);
        NavigationUtils.logoutAndCallLoginScreen(context, false);
        return true;
    }
}
