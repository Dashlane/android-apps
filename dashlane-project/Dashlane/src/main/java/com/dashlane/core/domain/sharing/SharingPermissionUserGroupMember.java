package com.dashlane.core.domain.sharing;

import com.dashlane.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({SharingPermissionUserGroupMember.ADMIN, SharingPermissionUserGroupMember.MEMBER,
        SharingPermissionUserGroupMember.REVOKED})
@Retention(RetentionPolicy.SOURCE)
public @interface SharingPermissionUserGroupMember {
    int ADMIN = R.string.user_group_member_permission_admin;
    int MEMBER = R.string.user_group_member_permission_member;
    int REVOKED = R.string.enum_sharing_permission_revoked;
}
