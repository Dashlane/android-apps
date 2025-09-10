package com.dashlane.xml.domain

import kotlin.reflect.KClass

val SyncObject.objectType: SyncObjectType
    get() = this::class.objectType

val KClass<out SyncObject>.objectType: SyncObjectType
    get() = SyncObjectType.forClass(this)

inline fun SyncObject.copy(block: SyncObject.Builder.() -> Unit): SyncObject =
    builder().apply(block).build()