package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException.AcceptUserCollectionRequestException
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AcceptCollectionRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {

    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    @Throws(AcceptUserCollectionRequestException::class)
    suspend fun build(collection: Collection): AcceptCollectionService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val collectionKey = sharingCryptography.getCollectionKeyFromUser(collection, login)
                ?: throw AcceptUserCollectionRequestException("Collection key is null.")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                collection.uuid,
                collectionKey.toByteArray()
            )
                ?: throw AcceptUserCollectionRequestException("Impossible to generate acceptation signature")
            AcceptCollectionService.Request(
                collectionId = UuidFormat(collection.uuid),
                revision = collection.revision,
                acceptSignature = acceptSignature
            )
        }
    }
}
