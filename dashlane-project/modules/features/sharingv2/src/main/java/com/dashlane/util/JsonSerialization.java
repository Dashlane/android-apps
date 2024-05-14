package com.dashlane.util;

import java.lang.reflect.Type;

public interface JsonSerialization {

    <T> T fromJson(String json, Class<T> classOfT) throws JsonException;

    <T> T fromJson(String json, Type typeOf) throws JsonException;

    String toJson(Object object);

    class JsonException extends RuntimeException {

        public JsonException(Throwable ex) {
            super(ex);
        }
    }
}
