


package com.dashlane.authenticator.util;



public class TotpCounter {

    

    private final long mTimeStep;

    

    public TotpCounter(long timeStep) {
        if (timeStep < 1) {
            throw new IllegalArgumentException("Time step must be positive: " + timeStep);
        }
        mTimeStep = timeStep;
    }

    

    public long getValueAtTime(long time) {
        assertValidTime(time);

        
        
        
        
        

        
        
        
        
        
        
        
        
        
        if (time >= 0) {
            return time / mTimeStep;
        } else {
            return (time - (mTimeStep - 1)) / mTimeStep;
        }
    }

    private static void assertValidTime(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("Negative time: " + time);
        }
    }
}