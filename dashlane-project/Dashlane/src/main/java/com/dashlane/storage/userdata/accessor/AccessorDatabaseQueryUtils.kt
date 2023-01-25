package com.dashlane.storage.userdata.accessor

import com.dashlane.lock.LockHelper
import com.dashlane.storage.userdata.accessor.filter.BaseFilter



fun LockHelper.forbidDataAccess(filter: BaseFilter? = null): Boolean =
    isLocked && filter != null && filter.requireUserUnlock