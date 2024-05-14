package com.dashlane.util.thread

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import java.util.concurrent.Executor

class ConflatedQueueExecutor : Executor {

    @OptIn(DelicateCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    private val actor = GlobalScope.actor<Runnable>(
        capacity = Channel.CONFLATED,
        start = CoroutineStart.UNDISPATCHED
    ) {
        for (command in channel) command.run()
    }

    override fun execute(command: Runnable) {
        actor.trySend(command)
    }
}