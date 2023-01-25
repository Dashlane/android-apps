package com.dashlane.core.premium;

import androidx.annotation.StringDef;



@StringDef({PremiumFeature.BACKUP_ONLY, PremiumFeature.SYNC, PremiumFeature.FREE})
public @interface PremiumFeature {
    String BACKUP_ONLY = "backup";
    String SYNC = "sync";
    String FREE = "";
}
