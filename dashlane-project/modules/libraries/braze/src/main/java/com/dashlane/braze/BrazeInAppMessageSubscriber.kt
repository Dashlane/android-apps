package com.dashlane.braze

import com.braze.events.IEventSubscriber
import com.braze.events.InAppMessageEvent

interface BrazeInAppMessageSubscriber : IEventSubscriber<InAppMessageEvent>