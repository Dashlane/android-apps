package com.dashlane.core.premium;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dashlane.core.premium.PremiumType.Type;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.premium.enddate.FormattedEndDate;
import com.dashlane.premium.enddate.FormattedEndDateProvider;
import com.dashlane.session.Session;
import com.dashlane.util.Constants;
import com.dashlane.util.StringUtils;
import com.dashlane.util.TextUtil;
import com.dashlane.util.userfeatures.UserFeaturesChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


public class PremiumStatus implements FormattedEndDateProvider {

    private static final int LIFETIME_THRESHOLD_IN_YEARS = 65;
    private static final String JSON_KEY_PREMIUM_STATUS_CODE = "statusCode";
    private static final String JSON_KEY_PREMIUM_EXPIRY_DATE = "endDate";
    private static final String JSON_KEY_PREMIUM_CURRENT_TIMESTAMP = "currentTimestamp";
    private static final String JSON_KEY_PREMIUM_AUTO_RENEW = "autoRenewal";
    private static final String JSON_KEY_TEAM_SPACES = "spaces";
    private static final String JSON_KEY_CAPABILITIES = "capabilities";
    private static final String JSON_KEY_CAPABILITIES_CAPABILITY = "capability";
    private static final String JSON_KEY_CAPABILITY_INFO = "info";
    private static final String JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA = "quota";
    private static final String JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA_REMAINING = "remaining";
    private static final String JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA_MAX = "max";
    private static final String JSON_KEY_FAMILY_MEMBERSHIP = "familyMembership";
    private static final String JSON_KEY_PLAYSTORE_SUBSCRIPTION_INFO = "playStoreSubscriptionInfo";
    private static final String JSON_KEY_AUTO_RENEW_INFO = "autoRenewInfo";
    private static final String JSON_KEY_PLAN_TYPE = "planType";
    private String mServerValue;

    private boolean mIsRefreshed = false;
    private boolean mIsAutoRenew;

    private String mPremiumExpiryDate;
    private PremiumType mPremiumType;
    private PremiumPlan mPremiumPlan;
    @Nullable
    private String mPlanType;
    private String mPremiumCurrentTimestamp;
    @Nullable
    private List<FamilyMembership> mFamilyMemberships;
    private PlayStoreSubscriptionInfo mPlayStoreSubscriptionInfo;
    private AutoRenewInfo mAutoRenewInfo;

    private JSONArray mTeamspaces;
    private JSONArray mCapabilities;
    
    private long mStorageRemaining;
    private long mStorageMax;
    private Clock mClock;

    public PremiumStatus() {
        mPremiumType = new PremiumType();
        mPremiumPlan = new PremiumPlan();
        mIsAutoRenew = false;
        mFamilyMemberships = null;
        mAutoRenewInfo = new AutoRenewInfo();
        mClock = SingletonProvider.getComponent().getClock();
    }

    public PremiumStatus(String response, boolean fresh) {
        this(response,
                fresh,
                SingletonProvider.getComponent().getClock(),
                SingletonProvider.getSessionManager().getSession(),
                SingletonProvider.getUserPreferencesManager());
    }

    public PremiumStatus(String response, boolean fresh, Clock clock, Session session, UserPreferencesManager userPreferencesManager) {
        mServerValue = response;
        mIsRefreshed = fresh;
        mIsAutoRenew = false;
        mPremiumType = new PremiumType();
        mPremiumPlan = new PremiumPlan();
        mFamilyMemberships = null;
        mAutoRenewInfo = new AutoRenewInfo();
        mClock = clock;
        try {
            if (StringUtils.isSemanticallyNull(response)) {
                StringBuilder builder = new StringBuilder("Premium status response attribute invalid - ");
                builder.append("fresh : ").append(fresh);
            } else {
                if (session == null) {
                    
                    return;
                }
                parsePremiumStatus(response);
                initPreferencesForMarketingMessageFromUserType(userPreferencesManager);
            }
        } catch (JSONException e) {
            mIsRefreshed = false;
        }
    }

    public boolean isPremium() {
        if (!mPremiumType.isPremiumType()) {
            return false;
        } else {
            return !hasExpired() && !hasExpiredCheckServerTimestamp();
        }
    }

    public boolean isLegacy() {
        return mPremiumType.isLegacyType();
    }

    private boolean hasExpiredCheckServerTimestamp() {
        if (mIsRefreshed && mPremiumCurrentTimestamp != null) {
            Instant expiry = getExpiryDate();
            Instant currentServerTime = getCurrentTimestampDate();
            return expiry.isBefore(currentServerTime);
        } else {
            return false;
        }
    }

    private boolean hasExpired() {
        Instant expiry = getExpiryDate();
        Instant today = Instant.now(mClock);
        return expiry.isBefore(today);
    }

    public Instant getExpiryDate() {
        Instant expiryDate = parseInstant(mPremiumExpiryDate);
        return expiryDate != null ? expiryDate : Instant.now(mClock);
    }

    @Nullable
    public Instant getEndDate() {
        return parseInstant(mPremiumExpiryDate);
    }

    private Instant getCurrentTimestampDate() {
        Instant expiryDate = parseInstant(mPremiumCurrentTimestamp);
        return expiryDate != null ? expiryDate : Instant.now(mClock);
    }

    public String getServerValue() {
        return mServerValue;
    }

    public PremiumType.Type getPremiumType() {
        return mPremiumType.getType();
    }

    public PremiumPlan getPremiumPlan() {
        return mPremiumPlan;
    }

    public boolean isRefreshed() {
        return mIsRefreshed;
    }

    public JSONArray getTeamspaces() {
        return mTeamspaces;
    }

    public JSONArray getCapabilities() {
        return mCapabilities;
    }

    public int getPremiumStatusCode() {
        return mPremiumType.getType().getJsonValue();
    }

    public boolean hasExpiryDateField() {
        return mPremiumType.getType() == Type.CURRENT_PREMIUM ||
               mPremiumType.getType() == Type.CANCELED_PREMIUM ||
               mPremiumType.getType() == Type.TRIAL ||
               mPremiumType.getType() == Type.GRACE;
    }

    private long remainingDaysForServer() {
        Duration duration = Duration.between(getCurrentTimestampDate(), getExpiryDate());
        if (duration.isNegative()) {
            return 0;
        } else {
            return duration.toDays();
        }
    }

    public long getRemainingDays() {
        return Math.min(remainingDaysForServer(), TextUtil.daysRemaining(getExpiryDate()));
    }

    public boolean isTrial() {
        return mPremiumType.getType() == PremiumType.Type.TRIAL && !hasExpired();
    }

    @Override
    public boolean willAutoRenew() {
        return mIsAutoRenew;
    }

    public long getStorageRemaining() {
        return mStorageRemaining;
    }

    public long getStorageMax() {
        return mStorageMax;
    }

    @Nullable
    public List<FamilyMembership> getFamilyMemberships() {
        return mFamilyMemberships;
    }

    public Boolean isFamilyUser() {
        return mFamilyMemberships != null && !mFamilyMemberships.isEmpty();
    }

    @Nullable
    public String getPlayStorePurchaseToken() {
        if (mPlayStoreSubscriptionInfo != null) {
            return mPlayStoreSubscriptionInfo.getPurchaseToken();
        } else {
            return null;
        }
    }

    @AutoRenewInfo.Periodicity
    public String getAutoRenewPeriodicity() {
        return mAutoRenewInfo.getFormattedPeriodicity();
    }

    @NonNull
    @Override
    public FormattedEndDate.Periodicity getFormattedAutoRenewPeriodicity() {
        switch (mAutoRenewInfo.getFormattedPeriodicity()) {
            case AutoRenewInfo.MONTHLY:
                return FormattedEndDate.Periodicity.MONTHLY;
            case AutoRenewInfo.YEARLY:
                return FormattedEndDate.Periodicity.YEARLY;
            default:
                return FormattedEndDate.Periodicity.UNDEFINED;
        }
    }

    @AutoRenewInfo.TriggerType
    public String getAutoRenewTrigger() {
        return mAutoRenewInfo.getFormattedTrigger();
    }

    @Nullable
    @Override
    public FormattedEndDate.Trigger getFormattedAutoRenewTrigger() {
        String trigger = mAutoRenewInfo.getFormattedTrigger();
        if (trigger == null) {
            return null;
        }
        switch (trigger) {
            case AutoRenewInfo.MANUAL:
                return FormattedEndDate.Trigger.MANUAL;
            case AutoRenewInfo.AUTOMATIC:
                return FormattedEndDate.Trigger.AUTOMATIC;
            default:
                return null;
        }
    }

    public boolean hasLifetimeEntitlement() {
        Instant endDateInstant = getEndDate();
        if (endDateInstant == null || endDateInstant == Instant.EPOCH) {
            return false;
        }
        LocalDateTime lifetimeThreshold = LocalDateTime.now().plusYears(LIFETIME_THRESHOLD_IN_YEARS);
        LocalDateTime endDate = LocalDateTime.ofInstant(endDateInstant, ZoneOffset.UTC);
        return endDate.isAfter(lifetimeThreshold);
    }

    @Nullable
    protected String getPlanType() {
        return mPlanType;
    }

    private void parsePremiumStatus(String response) throws JSONException {
        JSONObject status = new JSONObject(response);
        mPremiumType = new PremiumType(status.getInt(JSON_KEY_PREMIUM_STATUS_CODE));
        if (hasExpiryDateField() && status.has(JSON_KEY_PREMIUM_EXPIRY_DATE)) {
            mPremiumExpiryDate = status.getString(JSON_KEY_PREMIUM_EXPIRY_DATE);
        } else {
            mPremiumExpiryDate = null;
        }
        if (status.has(JSON_KEY_PREMIUM_AUTO_RENEW)) {
            mIsAutoRenew = status.getBoolean(JSON_KEY_PREMIUM_AUTO_RENEW);
        }
        if (status.has(JSON_KEY_PREMIUM_CURRENT_TIMESTAMP)) {
            mPremiumCurrentTimestamp = status.getString(JSON_KEY_PREMIUM_CURRENT_TIMESTAMP);
        }
        if (status.has(JSON_KEY_TEAM_SPACES)) {
            mTeamspaces = status.getJSONArray(JSON_KEY_TEAM_SPACES);
        }
        if (status.has(JSON_KEY_FAMILY_MEMBERSHIP)) {
            mFamilyMemberships =
                    FamilyMembership.fromJsonArray(status.getJSONArray(PremiumStatus.JSON_KEY_FAMILY_MEMBERSHIP));
        }
        if (status.has(JSON_KEY_PLAYSTORE_SUBSCRIPTION_INFO)) {
            mPlayStoreSubscriptionInfo = PlayStoreSubscriptionInfo.from(status, JSON_KEY_PLAYSTORE_SUBSCRIPTION_INFO);
        }
        if (status.has(JSON_KEY_AUTO_RENEW_INFO)) {
            mAutoRenewInfo = new AutoRenewInfo(status.optJSONObject(JSON_KEY_AUTO_RENEW_INFO));
        }
        if (status.has(JSON_KEY_PLAN_TYPE)) {
            mPlanType = status.optString(JSON_KEY_PLAN_TYPE);
        }
        mPremiumPlan = new PremiumPlan(status);
        parseCapabilities(status);
    }

    private void parseCapabilities(JSONObject status) {
        if (!status.has(JSON_KEY_CAPABILITIES)) {
            return;
        }
        extractCapabilities(status);
        for (int i = 0; i < mCapabilities.length(); i++) {
            try {
                parseCapability(mCapabilities.getJSONObject(i));
            } catch (JSONException e) {
                
            }
        }
    }

    private void parseCapability(JSONObject capability) throws JSONException {
        String capabilityName = capability.optString(JSON_KEY_CAPABILITIES_CAPABILITY);
        JSONObject capabilityInfo = capability.getJSONObject(JSON_KEY_CAPABILITY_INFO);
        if (UserFeaturesChecker.Capability.SECURE_FILES_UPLOAD.getValue().equals(capabilityName)) {
            parseCapabilitySecureFile(capabilityInfo);
        }
    }

    private void parseCapabilitySecureFile(JSONObject capabilityInfo) throws JSONException {
        JSONObject quota = capabilityInfo.getJSONObject(JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA);
        mStorageRemaining = quota.optLong(JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA_REMAINING);
        mStorageMax = quota.optLong(JSON_KEY_CAPABILITY_SECURE_FILES_INFO_QUOTA_MAX);
    }

    private void extractCapabilities(JSONObject status) {
        try {
            mCapabilities = status.getJSONArray(JSON_KEY_CAPABILITIES);
        } catch (JSONException e) {
            mCapabilities = new JSONArray();
        }
    }

    private Instant parseInstant(String time) {
        if (time == null) return null;
        try {
            return Instant.ofEpochSecond(Long.parseLong(time));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void initPreferencesForMarketingMessageFromUserType(UserPreferencesManager preferencesManager) {
        boolean premiumReminderExist = preferencesManager.exist(ConstantsPrefs.SHOW_PREMIUM_REMINDER);
        if (mPremiumType.getType() == Type.FREE && !premiumReminderExist) {
            preferencesManager.putBoolean(ConstantsPrefs.SHOW_PREMIUM_REMINDER, true);
            preferencesManager.putLong(ConstantsPrefs.TIMESTAMP_NEXT_PREMIUM_REMINDER,
                    System.currentTimeMillis()
                            + Constants.NB_DAYS_BEFORE_REMINDER_PREMIUM
                            * DateUtils.DAY_IN_MILLIS);
        }
    }
}
