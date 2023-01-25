package com.dashlane.util.inject;

import androidx.annotation.Nullable;



public interface OptionalProvider<T> {

    

    @Nullable
    T get();
}
