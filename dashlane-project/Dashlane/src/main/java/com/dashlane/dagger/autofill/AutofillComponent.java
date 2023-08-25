package com.dashlane.dagger.autofill;

import android.content.Context;

import com.dashlane.autofill.accessibility.DashlaneAccessibilityService;
import com.dashlane.dagger.CoroutinesModule;
import com.dashlane.dagger.FeatureScope;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.ui.InAppLoginWindow;

import dagger.Component;

@FeatureScope
@Component(dependencies = SingletonComponentProxy.class,
           modules = {AutofillModule.class, CoroutinesModule.class})
public interface AutofillComponent {

    void inject(DashlaneAccessibilityService accessibilityService);

    void inject(InAppLoginWindow inAppLoginWindow);

    final class ComponentProvider {
        static AutofillComponent sComponent;

        private ComponentProvider() {
            throw new UnsupportedOperationException();
        }

        public static AutofillComponent get(Context context) {
            if (sComponent == null) {
                SingletonProvider.init(context);
                sComponent = DaggerAutofillComponent.builder()
                                                    .singletonComponentProxy(
                                                            SingletonProvider.getComponent())
                                                    .build();
            }
            return sComponent;
        }
    }

}
