package com.dashlane.ui.activities.firstpassword.autofilldemo

import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class AutofillDemoDataProvider @Inject constructor() : BaseDataProvider<AutofillDemo.Presenter>(),
    AutofillDemo.DataProvider