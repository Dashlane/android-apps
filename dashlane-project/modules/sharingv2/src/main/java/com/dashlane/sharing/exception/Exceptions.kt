package com.dashlane.sharing.exception

open class SharingException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)

sealed class RequestBuilderException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : SharingException(message, cause) {
    class AcceptItemGroupRequestException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : RequestBuilderException(message, cause)

    class AcceptUserGroupRequestException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : RequestBuilderException(message, cause)

    class CreateItemRequestException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : RequestBuilderException(message, cause)

    class InviteItemGroupMembersRequestException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : RequestBuilderException(message, cause)
}

class SharingResponseException(message: String) : SharingException(message)

class SharingAlreadyAccessException : SharingException("You already have access to this item.")
