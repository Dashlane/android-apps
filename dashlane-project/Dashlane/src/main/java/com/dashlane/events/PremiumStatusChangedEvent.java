package com.dashlane.events;

import com.dashlane.core.premium.PremiumPlan;
import com.dashlane.core.premium.PremiumStatus;
import com.dashlane.event.AppEvent;

import java.util.Objects;

public class PremiumStatusChangedEvent implements AppEvent {

    private PremiumStatus mPreviousStatus;
    private PremiumStatus mNewStatus;

    public PremiumStatusChangedEvent(PremiumStatus previousStatus, PremiumStatus newStatus) {
        mPreviousStatus = previousStatus;
        mNewStatus = newStatus;
    }

    public PremiumStatus getPreviousStatus() {
        return mPreviousStatus;
    }

    public PremiumStatus getNewStatus() {
        return mNewStatus;
    }

    public boolean premiumPlanChanged() {
        if (mPreviousStatus == null || mNewStatus == null) {
            return mPreviousStatus != mNewStatus; 
        }
        PremiumPlan previousPremiumPlan = mPreviousStatus.getPremiumPlan();
        PremiumPlan newPremiumPlan = mNewStatus.getPremiumPlan();
        if (previousPremiumPlan == null || newPremiumPlan == null) {
            return previousPremiumPlan != newPremiumPlan; 
        }
        return !Objects.equals(previousPremiumPlan.getName(), newPremiumPlan.getName())
               || !Objects.equals(previousPremiumPlan.getType(), newPremiumPlan.getType())
               || !Objects.equals(previousPremiumPlan.getFeature(), newPremiumPlan.getFeature());
    }

}
