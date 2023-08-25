package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.lock.LockFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter

interface BaseFilter : SpaceFilter, DataTypeFilter, LockFilter, StatusFilter