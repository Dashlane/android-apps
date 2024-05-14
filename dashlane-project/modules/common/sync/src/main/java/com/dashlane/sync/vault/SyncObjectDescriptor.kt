package com.dashlane.sync.vault

import com.dashlane.xml.domain.SyncObject
import kotlin.reflect.KClass

typealias SyncObjectDescriptor = Pair<KClass<out SyncObject>, String>