package com.dashlane.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GsonJsonSerialization implements JsonSerialization {

    private final Gson mGson;

    public GsonJsonSerialization(Gson gson) {
        mGson = gson;
    }

    @Override
    public <T> T fromJson(String json, Type typeOf) throws JsonException {
        try {
            return mGson.fromJson(json, typeOf);
        } catch (JsonSyntaxException ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) throws JsonException {
        try {
            return mGson.fromJson(json, classOfT);
        } catch (JsonSyntaxException ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public String toJson(Object object) {
        return mGson.toJson(object);
    }
}
