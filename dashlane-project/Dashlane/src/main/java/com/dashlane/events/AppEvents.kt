package com.dashlane.events

import com.dashlane.event.AppEvent
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEvents @Inject constructor(
    private val sessionManager: SessionManager,
    private val scope: SessionCoroutineScopeRepository,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) {
    private val listenersMapByEventClassName = ConcurrentHashMap<String, MutableMap<Any, (AppEvent) -> Unit>>()
    private val lastEventByEventClassName = ConcurrentHashMap<String, AppEvent>()

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class)
    fun <T : AppEvent> register(
        subscriber: Any,
        clazz: Class<T>,
        deliverLastEvent: Boolean = false,
        listener: (T) -> Unit
    ) {
        val listenerBySubscriberClassName = listenersMapByEventClassName.getOrPut(clazz.name) { mutableMapOf() }
        val existingListener = listenerBySubscriberClassName[subscriber]
        if (existingListener != null && listener != existingListener) {
            throw IllegalStateException("Subscriber has already registered a distinct listener for this event")
        }
        listenerBySubscriberClassName[subscriber] = listener as (AppEvent) -> Unit

        if (deliverLastEvent) deliverLastEvent(subscriber, clazz)
    }

    @Throws(IllegalStateException::class)
    fun <T : AppEvent> unregister(subscriber: Any, clazz: Class<T>) {
        val listenerBySubscriberClassName = listenersMapByEventClassName[clazz.name]
            ?: throw IllegalStateException("Subscriber is not registered for this event")
        listenerBySubscriberClassName.remove(subscriber)
    }

    fun unregisterAll() = listenersMapByEventClassName.clear()

    fun <T : AppEvent> post(event: T) {
        val session = sessionManager.session ?: return
        val type = event::class.java.name
        lastEventByEventClassName[type] = event
        val listeners = listenersMapByEventClassName[type]?.values?.toList() ?: return
        scope.getCoroutineScope(session).launch(mainDispatcher) {
            listeners.forEach {
                it.invoke(event)
            }
        }
    }

    fun <T : AppEvent> clearLastEvent(clazz: Class<T>) {
        lastEventByEventClassName.remove(clazz.name)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AppEvent> getLastEvent(clazz: Class<T>) = lastEventByEventClassName[clazz.name] as? T

    private fun <T : AppEvent> deliverLastEvent(subscriber: Any, clazz: Class<T>) {
        val session = sessionManager.session ?: return
        lastEventByEventClassName[clazz.name]?.let {
            val listenerBySubscriberClassName = listenersMapByEventClassName[clazz.name] ?: return@let
            val listener = listenerBySubscriberClassName[subscriber] ?: return@let
            scope.getCoroutineScope(session).launch(mainDispatcher) {
                listener.invoke(it)
            }
        }
    }
}

inline fun <reified T : AppEvent> AppEvents.register(
    subscriber: Any,
    deliverLastEvent: Boolean = false,
    noinline listener: (T) -> Unit
) {
    register(subscriber, T::class.java, deliverLastEvent, listener)
}

inline fun <reified T : AppEvent> AppEvents.unregister(subscriber: Any) {
    unregister(subscriber, T::class.java)
}

inline fun <reified T : AppEvent> AppEvents.getLastEvent() = getLastEvent(T::class.java)

inline fun <reified T : AppEvent> AppEvents.clearLastEvent() = clearLastEvent(T::class.java)

inline fun <reified T : AppEvent> AppEvents.registerAsFlow(
    subscriber: Any,
    clazz: Class<T>,
    deliverLastEvent: Boolean = false,
) = callbackFlow {
    register(
        subscriber = subscriber,
        clazz = clazz,
        deliverLastEvent = deliverLastEvent
    ) { trySendBlocking(it) }
    awaitClose {
        unregister(
            subscriber,
            clazz = clazz
        )
    }
}