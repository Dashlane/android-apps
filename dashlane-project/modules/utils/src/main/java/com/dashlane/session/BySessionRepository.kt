package com.dashlane.session



interface BySessionRepository<T> {
    operator fun get(session: Session?): T?
}