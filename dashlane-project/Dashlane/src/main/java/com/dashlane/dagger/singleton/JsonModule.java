package com.dashlane.dagger.singleton;

import com.dashlane.util.GsonJsonSerialization;
import com.dashlane.util.JsonSerialization;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;



@Module
public class JsonModule {

    @Provides
    @Singleton
    public JsonSerialization provideJsonSerialization() {
        return new GsonJsonSerialization(new Gson());
    }
}
