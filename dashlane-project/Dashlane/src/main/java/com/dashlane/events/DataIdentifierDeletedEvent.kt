package com.dashlane.events

import com.dashlane.event.AppEvent
import com.dashlane.xml.domain.SyncObjectType



class DataIdentifierDeletedEvent(val type: SyncObjectType) : AppEvent