package com.dashlane.teamspaces.manager;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.vault.model.AuthentifiantKt;
import com.dashlane.vault.model.SpaceItemUtilKt;
import com.dashlane.vault.model.VaultItem;
import com.dashlane.vault.util.TeamSpaceUtils;
import com.dashlane.xml.domain.SyncObject;

import java.util.List;

public class TeamspaceValidator {

    private TeamspaceValidator() {
        
    }

    public static boolean isValidItem(VaultItem sharedObject) {
        if (sharedObject == null) {
            return false;
        }

        if (!SpaceItemUtilKt.isSpaceItem(sharedObject)) {
            return true;
        }

        Teamspace teamspace;
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            TeamspaceManager teamManager =
                    SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
            teamspace = teamManager.get(TeamSpaceUtils.getTeamSpaceId(sharedObject));
        } else {
            return false;
        }
        
        return teamspace != null
               && (Teamspace.Status.ACCEPTED.equals(teamspace.getStatus())
                   || (Teamspace.Status.REVOKED.equals(teamspace.getStatus())
                       && !isForceCategorise(teamspace, sharedObject)));
    }

    private static boolean isForceCategorise(Teamspace teamspace, VaultItem vaultItem) {
        SyncObject item = vaultItem.getSyncObject();
        if (teamspace == null || !teamspace.isDomainRestrictionsEnable()) return false;
        List<String> domains = teamspace.getDomains();
        if (domains.isEmpty()) return false;

        return (
                       item instanceof SyncObject.Authentifiant &&
                       isForceCategoriseAuthentifiant(domains, (SyncObject.Authentifiant) item)
               ) ||
               (
                       item instanceof SyncObject.Email &&
                       isForceCategoriseEmail(domains, (SyncObject.Email) item)
               );
    }

    private static boolean isForceCategoriseAuthentifiant(List<String> domains,
                                                          SyncObject.Authentifiant authentifiant) {
        String[] fields = new String[]{AuthentifiantKt.getNavigationUrl(authentifiant), AuthentifiantKt.getLoginForUi(
                authentifiant)};
        for (String domain : domains) {
            for (String field : fields) {
                if (field != null && field.contains(domain)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isForceCategoriseEmail(List<String> domains, SyncObject.Email email) {
        String emailAddress = email.getEmail();
        for (int i = 0; i < domains.size() && emailAddress != null; i++) {
            if (emailAddress.contains(domains.get(i))) {
                return true;
            }
        }
        return false;
    }
}
