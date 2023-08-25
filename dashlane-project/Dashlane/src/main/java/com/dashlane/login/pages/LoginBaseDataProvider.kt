package com.dashlane.login.pages

import com.skocken.presentation.provider.BaseDataProvider

abstract class LoginBaseDataProvider<T : LoginBaseContract.Presenter> :
    BaseDataProvider<T>(),
    LoginBaseContract.DataProvider