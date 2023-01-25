package com.dashlane.autofill;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;



@IntDef({AutofillOrigin.IN_APP_LOGIN,
         AutofillOrigin.AUTO_FILL_API,
         AutofillOrigin.INLINE_AUTOFILL_KEYBOARD})
@Retention(RetentionPolicy.SOURCE)
public @interface AutofillOrigin {
    int IN_APP_LOGIN = 0;
    int AUTO_FILL_API = 1;
    int INLINE_AUTOFILL_KEYBOARD = 2;
}
