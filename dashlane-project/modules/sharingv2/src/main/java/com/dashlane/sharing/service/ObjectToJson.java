package com.dashlane.sharing.service;

import com.dashlane.util.JsonSerialization;

import androidx.annotation.VisibleForTesting;



public class ObjectToJson<T> {

    private final T mObject;
    private final JsonSerialization mJsonSerialization;

    public ObjectToJson(T object, JsonSerialization jsonSerialization) {
        mObject = object;
        mJsonSerialization = jsonSerialization;
    }

    @VisibleForTesting
    public T getObject() {
        return mObject;
    }

    @Override
    public String toString() {
        return mJsonSerialization.toJson(mObject);
    }
}
