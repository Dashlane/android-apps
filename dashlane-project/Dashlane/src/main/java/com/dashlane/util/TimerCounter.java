package com.dashlane.util;

public class TimerCounter {

    private long mStartTime;
    private long mEndTime;

    public TimerCounter() {
        super();
        mStartTime = 0;
        mEndTime = 0;
    }

    public TimerCounter start() {
        mStartTime = System.nanoTime();
        return this;
    }

    public TimerCounter stop() {
        mEndTime = System.nanoTime();
        return this;
    }

    public long getDurationMs() {
        if (mStartTime == 0) {
            return 0;
        }
        long endTime;
        if (mEndTime == 0) {
            endTime = System.nanoTime();
        } else {
            endTime = mEndTime;
        }
        return (endTime - mStartTime) / 1_000_000; 
    }
}
