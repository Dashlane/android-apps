package com.dashlane.security;

import com.dashlane.dagger.singleton.DomainCategoryModule;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

import dagger.Component;



public class UsageLogCode12SecurityScoreFactory {

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsageLogCode12SecurityScoreScope {
    }

    @UsageLogCode12SecurityScoreScope
    @Component(dependencies = SingletonComponentProxy.class,
               modules = {DomainCategoryModule.class})
    public interface UsageLogCode12SecurityComponent {
        UsageLogCode12SecurityScore getUsageLogCode12SecurityScoreFactory();
    }

    public static UsageLogCode12SecurityScore newInstance() {
        return DaggerUsageLogCode12SecurityScoreFactory_UsageLogCode12SecurityComponent
                .builder()
                .singletonComponentProxy(SingletonProvider.getComponent())
                .build()
                .getUsageLogCode12SecurityScoreFactory();
    }

}
