package com.dashlane.core.premium;

public class PremiumType {

    private Type mType;

    PremiumType() {
        mType = Type.FREE;
    }

    PremiumType(int type) {
        setType(type);
    }

    public Type getType() {
        return mType;
    }

    private void setType(int type) {
        boolean typeSet = false;
        int i = 1;
        while (i < Type.values().length && !typeSet) {
            if (Type.values()[i].getJsonValue() == type) {
                mType = Type.values()[i];
                typeSet = true;
            }
            i++;
        }
        if (!typeSet) {
            mType = Type.UNKNOWN;
        }
    }

    boolean isPremiumType() {
        return mType == Type.CURRENT_PREMIUM ||
               mType == Type.CANCELED_PREMIUM;
    }

    boolean isLegacyType() {
        return mType == Type.LEGACY;
    }

    public enum Type {
        UNKNOWN(-1),
        FREE(0),
        CURRENT_PREMIUM(1),
        CANCELED_PREMIUM(2),
        LEGACY(3),
        TRIAL(4),
        GRACE(5);

        private int mJsonValue;

        Type(int jsonValue) {
            mJsonValue = jsonValue;
        }

        public int getJsonValue() {
            return mJsonValue;
        }
    }


}
