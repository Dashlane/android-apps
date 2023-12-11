package com.dashlane.teamspaces.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;

public class Teamspace {

    
    public static final String DEFAULT_SPACE_ANON_ID = "SPACE_DEFAULT";
    
    public static final String ALL_SPACE_ANON_ID = "SPACE_ALL";

    public static final String SENTINEL_DEFAULT_ANON_ID = "ANON_ID_NOT_SET";
    @VisibleForTesting
    @SerializedName("info")
    Map<String, Object> mInfo;
    @SerializedName("teamId")
    private String mTeamId;
    @SerializedName("teamName")
    private String mTeamName;
    @SerializedName("companyName")
    private String mCompanyName;
    @SerializedName("letter")
    private String mDisplayLetter;
    @SerializedName("color")
    private String mColor;
    @SerializedName("joinDate")
    private long mDateJoined;
    @SerializedName("invitationDate")
    private long mDateInvited;
    @SerializedName("revokeDate")
    private long mRevokedDate;
    @SerializedName("planType")
    private String mPlanType;
    @SerializedName("status")
    private String mStatus;
    @SerializedName("shouldDelete")
    private boolean mShouldDelete;
    @SerializedName("isSSOUser")
    private boolean mIsSsoUser;
    private int mColorInt = -1;
    private int mType = Type.COMPANY;
    private String mAnonTeamId = SENTINEL_DEFAULT_ANON_ID; 

    private List<String> mTeamsDomains;

    @Nullable
    public String getTeamId() {
        return mTeamId;
    }

    public void setTeamId(String teamId) {
        mTeamId = teamId;
    }

    public String getAnonTeamId() {
        return mAnonTeamId;
    }

    public void setAnonTeamId(String anonSpaceId) {
        mAnonTeamId = anonSpaceId;
    }

    @Nullable
    public String getTeamName() {
        return mTeamName;
    }

    public void setTeamName(String teamName) {
        mTeamName = teamName;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String companyName) {
        mCompanyName = companyName;
    }

    public String getDisplayLetter() {
        if (mDisplayLetter != null && mDisplayLetter.length() > 1) {
            return mDisplayLetter.substring(0, 1);
        }
        return mDisplayLetter;
    }

    public void setDisplayLetter(String displayLetter) {
        mDisplayLetter = displayLetter;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
        mColorInt = -1;
    }

    public int getColorInt() {
        if (mColorInt == -1) {
            try {
                mColorInt = Color.parseColor(mColor);
            } catch (Exception ex) {
                mColorInt = Color.TRANSPARENT;
            }
        }
        return mColorInt;
    }

    public long getDateJoined() {
        return mDateJoined;
    }

    public void setDateJoined(long dateJoined) {
        mDateJoined = dateJoined;
    }

    public long getDateInvited() {
        return mDateInvited;
    }

    public void setDateInvited(long dateInvited) {
        mDateInvited = dateInvited;
    }

    public long getRevokedDate() {
        return mRevokedDate;
    }

    public void setRevokedDate(long revokedDate) {
        mRevokedDate = revokedDate;
    }

    @Plan
    public String getPlanType() {
        return mPlanType;
    }

    public void setPlanType(@Plan String planType) {
        mPlanType = planType;
    }

    @Status
    @Nullable
    public String getStatus() {
        return mStatus;
    }

    public void setStatus(@Status String status) {
        mStatus = status;
    }

    @Type
    public int getType() {
        return mType;
    }

    protected void setType(@Type int type) {
        mType = type;
    }

    public Map<String, Object> getTeamspaceInfo() {
        return mInfo;
    }

    public void setTeamspaceInfo(Map<String, Object> teamspaceInfo) {
        mInfo = teamspaceInfo;
    }

    public boolean featureDisabledForSpace(@Feature String featureName) {
        Map<String, Object> info = getTeamspaceInfo();
        if (info != null && info.containsKey(featureName)) {
            return Boolean.parseBoolean(info.get(featureName).toString());
        }
        
        return false;
    }

    @Nullable
    public String getFeatureValue(@Feature String featureName) {
        Map<String, Object> info = getTeamspaceInfo();
        if (info != null && info.containsKey(featureName)) {
            Object feature = info.get(featureName);
            return feature instanceof String ? (String) feature : null;
        }
        
        return null;
    }

    @NonNull
    public List<String> getDomains() {
        if (mTeamsDomains == null) {
            mTeamsDomains = new ArrayList<>();
            if (mInfo != null) {
                Object teamDomains = mInfo.get("teamDomains");
                if (teamDomains instanceof List) {
                    List teamListDomains = (List) teamDomains;
                    for (int i = 0; i < teamListDomains.size(); i++) {
                        Object domain = teamListDomains.get(i);
                        if (domain instanceof String) {
                            mTeamsDomains.add((String) domain);
                        }
                    }
                }
            }
        }
        return mTeamsDomains;
    }

    @NonNull
    public List<String> getDomainsToExcludeNow() {
        if (!Status.REVOKED.equals(mStatus)
            || !isRemoveForcedContentEnabled()) {
            
            return new ArrayList<>();
        } else {
            return getDomains();
        }
    }

    public boolean canBeDisplay() {
        return Type.COMBINED != mType
               && (Status.ACCEPTED.equals(mStatus) ||
                   (Status.REVOKED.equals(mStatus) && !shouldDeleteForceCategorizedContent()));
    }


    public boolean isDomainRestrictionsEnable() {
        if (mInfo == null) {
            return false;
        }
        Object restrictionsEnabled = mInfo.get("forcedDomainsEnabled");
        return restrictionsEnabled instanceof Boolean && (Boolean) restrictionsEnabled;
    }

    public boolean shouldDeleteForceCategorizedContent() {
        return mShouldDelete && isRemoveForcedContentEnabled();
    }

    public boolean isRemoveForcedContentEnabled() {
        if (mInfo == null) {
            return false;
        }
        Object removeEnable = mInfo.get("removeForcedContentEnabled");
        return removeEnable instanceof Boolean && (Boolean) removeEnable;
    }

    public boolean shouldDelete() {
        return mShouldDelete;
    }

    public void setShouldDelete(boolean shouldDelete) {
        mShouldDelete = shouldDelete;
    }

    public boolean isSsoUser() {
        return mIsSsoUser;
    }

    public boolean isCollectSensitiveDataAuditLogsEnabled() {
        if (mInfo == null) {
            return false;
        }
        Object collectEnable = mInfo.get("collectSensitiveDataAuditLogsEnabled");
        return collectEnable instanceof Boolean && (Boolean) collectEnable;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teamspace teamspace = (Teamspace) o;
        return mDisplayLetter == teamspace.getDisplayLetter() &&
               mDateJoined == teamspace.getDateJoined() &&
               mDateInvited == teamspace.getDateInvited() &&
               mRevokedDate == teamspace.getRevokedDate() &&
               Objects.equals(mTeamId, teamspace.getTeamId()) &&
               Objects.equals(mTeamName, teamspace.getTeamName()) &&
               Objects.equals(mCompanyName, teamspace.getCompanyName()) &&
               Objects.equals(mColor, teamspace.getColor()) &&
               Objects.equals(mPlanType, teamspace.getPlanType()) &&
               Objects.equals(mStatus, teamspace.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getTeamId(),
                getTeamName(),
                getCompanyName(),
                getDisplayLetter(),
                getColor(),
                getDateJoined(),
                getDateInvited(),
                getRevokedDate(),
                getPlanType(),
                getStatus());
    }

    @Override
    public String toString() {
        return "Teamspace [teamId=" + mTeamId
               + ", teamName=" + mTeamName
               + ", companyName=" + mCompanyName
               + ", letter=" + mDisplayLetter
               + ", color=" + mColor
               + ", joinDate=" + mDateJoined
               + ", invitationDate=" + mDateInvited
               + ", revokeDate=" + mRevokedDate
               + ", planType=" + mPlanType
               + ", status=" + mStatus + "]";
    }

    @StringDef({Plan.TRIAL, Plan.TEAM, Plan.PERSONAL})
    @Retention(RetentionPolicy.CLASS)
    public @interface Plan {
        String TRIAL = "teamTrial";
        String TEAM = "teamOffer";
        String PERSONAL = "personal";
    }

    @StringDef({Status.INVITED, Status.ACCEPTED, Status.DECLINED, Status.REVOKED,
                Status.REMOVED})
    @Retention(RetentionPolicy.CLASS)
    public @interface Status {
        String INVITED = "invited";
        String DECLINED = "declined";
        String ACCEPTED = "accepted";
        String REVOKED = "revoked";
        String REMOVED = "removed";
    }

    @IntDef({Type.PERSONAL, Type.COMBINED, Type.COMPANY})
    @Retention(RetentionPolicy.CLASS)
    public @interface Type {
        int PERSONAL = -1;
        int COMBINED = 0;
        int COMPANY = 1;
    }

    @StringDef({Feature.SHARING_DISABLED, Feature.EMERGENCY_DISABLED, Feature.SECURE_NOTES_DISABLED, Feature.AUTOLOCK,
                Feature.CRYPTO_FORCED_PAYLOAD, Feature.ENFORCED_2FA})
    @Retention(RetentionPolicy.CLASS)
    public @interface Feature {
        String SHARING_DISABLED = "sharingDisabled";
        String SECURE_NOTES_DISABLED = "secureNotesDisabled";
        String EMERGENCY_DISABLED = "emergencyDisabled";
        String AUTOLOCK = "lockOnExit";
        String CRYPTO_FORCED_PAYLOAD = "cryptoForcedPayload";
        String ENFORCED_2FA = "twoFAEnforced";
    }
}
