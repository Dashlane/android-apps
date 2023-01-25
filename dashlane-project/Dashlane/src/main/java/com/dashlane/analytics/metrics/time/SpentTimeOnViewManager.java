package com.dashlane.analytics.metrics.time;

import com.dashlane.analytics.metrics.time.model.TimeSpent;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;



public class SpentTimeOnViewManager {

    final private static Duration SIGNIFICANT_TIME_SPENT = Duration.ofSeconds(1);
    private static SpentTimeOnViewManager mInstance;
    private HashMap<String, TimeSpent> mViewsMap;

    private SpentTimeOnViewManager() {
        super();
        mViewsMap = new HashMap<>();
    }

    public static SpentTimeOnViewManager getInstance() {
        if (mInstance == null) {
            mInstance = new SpentTimeOnViewManager();
        }
        return mInstance;
    }

    public void enterView(String viewName) {
        mViewsMap.put(viewName, new TimeSpent(Instant.now()));
    }

    public void leaveView(String viewName) {
        TimeSpent ts = mViewsMap.get(viewName);
        if (ts != null) {
            ts.leave();
            Duration spentTime = ts.computeTimeSpent();
            sendUsageLogIfSignificantSpentTime(viewName, spentTime);
            mViewsMap.remove(viewName);
        }
    }

    private void sendUsageLogIfSignificantSpentTime(String viewName, Duration spentTime) {
        if (viewName != null && spentTime.compareTo(SIGNIFICANT_TIME_SPENT) > 0) {
            SingletonComponentProxy singletonComponent = SingletonProvider.getComponent();
            new SpentTimeOnViewLogger(
                    singletonComponent.getSessionManager(),
                    singletonComponent.getBySessionUsageLogRepository(),
                    singletonComponent.getTeamspaceRepository()
            ).log(viewName, spentTime.toMillis() / 1000);
        }
    }
}