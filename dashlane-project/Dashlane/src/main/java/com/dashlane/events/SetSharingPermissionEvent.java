package com.dashlane.events;

import com.dashlane.core.domain.sharing.SharingPermission;
import com.dashlane.event.AppEvent;

public class SetSharingPermissionEvent implements AppEvent {

    private SharingPermission mPermission;

    public SetSharingPermissionEvent(SharingPermission mPermission) {
        this.mPermission = mPermission;
    }

    public SharingPermission getPermission() {
        return mPermission;
    }
}
