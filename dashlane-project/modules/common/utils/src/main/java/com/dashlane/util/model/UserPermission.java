package com.dashlane.util.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

@StringDef({UserPermission.ADMIN, UserPermission.LIMITED, UserPermission.UNDEFINED})
@Retention(RetentionPolicy.SOURCE)
public @interface UserPermission {
    String ADMIN = "admin";
    String LIMITED = "limited";
    String UNDEFINED = "undefined";
}
