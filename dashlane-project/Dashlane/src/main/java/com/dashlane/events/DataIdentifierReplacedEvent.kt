package com.dashlane.events

import com.dashlane.event.AppEvent

class DataIdentifierReplacedEvent(val oldItemId: String, val newItemId: String) : AppEvent