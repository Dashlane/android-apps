package com.dashlane.util;

import com.dashlane.BuildConfig;
import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.notification.model.TokenNotificationHandler;
import com.dashlane.xml.domain.SyncObjectType;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Constants {
    static final String URL_VALID_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;" +
                                          "]*[-a-zA-Z0-9+&@#/%=~_|]";

    public static final int NB_DAYS_BEFORE_REMINDER_PREMIUM = 7;
    public static final String DEFAULT_TEAMSPACE = "default_space";
    public static final String DEFAULT_TEAMSPACE_TYPE = "default_space_type";
    public static final String DEFAULT_TEAMSPACE_ID = "default_space_id";

    private Constants() {
        
    }

    public static final long PREMIUM_NEW_DEVICE_RECURRING_DELAY_MS = TimeUnit.DAYS.toMillis(15);
    public static final int PREMIUM_NEW_DEVICE_RECURRING_MAX_COUNT = 3;

    public static String getLang() {
        try {
            return SingletonProvider.getContext().getString(R.string.language_iso_639_1);
        } catch (Exception e) {
            return "EN";
        }
    }

    public static String getOSLang() {
        return Locale.getDefault().getLanguage().substring(0, 2).toLowerCase(Locale.US);
    }

    public enum Package {
        internal, external, world
    }

    public static class HTTP {
        static final String IconsUrl0 = "https://s3-eu-west-1.amazonaws.com/static-icons/";
        static final String RefferalUrlHeader = "https://www.dashlane.com/";

        private HTTP() {
            
        }
    }

    public static final class INDEX {
        public static final CharSequence ALPHAINDEX_NO_SPECIAL_CHAR = " 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private INDEX() {
            
        }
    }

    public static class BROADCASTS {

        public static final String PASSWORD_SUCCESS_BROADCAST = "com.dashlane.PASSWORD";
        public static final String NEW_TOKEN_BROADCAST = "com.dashlane.NEW_TOKEN";
        public static final String SUCCESS_EXTRA = "success";
        public static final String SYNCFINISHED_BROADCAST = "com.dashlane.SYNCFINISH";
        public static final String SYNC_PROGRESS_BROADCAST = "com.dashlane.SYNC_PROGRESS_SHOW";
        public static final String SYNC_PROGRESS_BROADCAST_SHOW_PROGRESS = SYNC_PROGRESS_BROADCAST + ".showProgress";

        private BROADCASTS() {
            
        }
    }

    public static class WORKMODE {
        public static final int MAXIMUM_UNLOCK_TRIES = 3;
        public static boolean offline = false;

        private WORKMODE() {
            
        }
    }

    public static class MARKETING {
        public static final String ORIGIN = "origin";
        public static final String SHOULD_SEND_REPORTS = "Send";
        public static final String CONTENT = "content";
        public static final String REFFERAL_STRING = "refferal_string";

        
        public static final String LANGUAGE = "language";

        public static final String ADJUST_API_KEY = BuildConfig.ADJUST_API_KEY;
        public static final String ADJUST_EVENT_INSTALL = "5afa9x";

        private MARKETING() {
            
        }
    }

    public static class TIME {
        public static long LOGIN_TIME_SECONDS = 0;
        public static long LOGOUT_TIME_SECONDS = 0;

        private TIME() {
            
        }
    }

    public static class IN_APP_BILLING {
        public static final String PLATFORM_PLAY_STORE_SUBSCRIPTION = "playstore_subscription";

        private IN_APP_BILLING() {
            
        }
    }

    public static class GCM {
        public static final String CLEAR_GCM_NOTIFICATION = "com.dashlane.gcm.CLEAR_NOTIFICATIONS";

        public static final HashMap<String, TokenNotificationHandler> Token =
                new HashMap<String, TokenNotificationHandler>();
        public static final HashMap<String, Boolean> TokenShouldNotify = new HashMap<String, Boolean>();
        private GCM() {
            
        }
    }

    public static class PREMIUM {
        public static final int RENEWAL_REMAINING_FIRST_NOTIFICATION = 25;
        public static final int RENEWAL_REMAINING_SECOND_NOTIFICATION = 5;
        public static final int RENEWAL_REMAINING_THIRD_NOTIFICATION = 1;

        private PREMIUM() {
            
        }
    }

    public static class MENU_ORDERS {

        public static final SyncObjectType[] IDS_ORDER = new SyncObjectType[]{
                SyncObjectType.ID_CARD,
                SyncObjectType.PASSPORT,
                SyncObjectType.DRIVER_LICENCE,
                SyncObjectType.SOCIAL_SECURITY_STATEMENT,
                SyncObjectType.FISCAL_STATEMENT
        };

        public static final SyncObjectType[] CONTACTS_ORDER = new SyncObjectType[]{
                SyncObjectType.IDENTITY,
                SyncObjectType.EMAIL,
                SyncObjectType.PHONE,
                SyncObjectType.ADDRESS,
                SyncObjectType.COMPANY,
                SyncObjectType.PERSONAL_WEBSITE
        };

        public static final SyncObjectType[] PAYMENTS_ORDER = new SyncObjectType[]{
                SyncObjectType.PAYMENT_CREDIT_CARD,
                SyncObjectType.PAYMENT_PAYPAL,
                SyncObjectType.BANK_STATEMENT
        };

        private MENU_ORDERS() {
            
        }
    }

    public static class TESTS {
        public static boolean IS_TESTING_BUILD = false;
        public static String TRACE_LOGS_FILENAME = null;

        private TESTS() {
            
        }
    }
}
