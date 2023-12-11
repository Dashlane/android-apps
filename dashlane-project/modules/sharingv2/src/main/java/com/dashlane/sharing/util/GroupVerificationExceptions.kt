package com.dashlane.sharing.util

sealed class KeyNotFoundException : Exception() {
    sealed class KeyNotFoundItemGroupException : KeyNotFoundException() {
        class KeyNotFoundUserException : KeyNotFoundItemGroupException()
        class KeyNotFoundUserGroupException : KeyNotFoundItemGroupException()
        class KeyNotFoundCollectionException : KeyNotFoundItemGroupException()
    }

    sealed class KeyNotFoundUserGroupException : KeyNotFoundException() {
        class KeyNotFoundUserException : KeyNotFoundUserGroupException()
    }

    sealed class KeyNotFoundCollectionException : KeyNotFoundException() {
        class KeyNotFoundUserException : KeyNotFoundCollectionException()
        class KeyNotFoundUserGroupException : KeyNotFoundCollectionException()
    }
}

sealed class KeyNotVerifiedException : Exception() {
    sealed class KeyNotVerifiedItemGroupException : KeyNotVerifiedException() {
        class KeyNotVerifiedUserException : KeyNotVerifiedItemGroupException()
        class KeyNotVerifiedUserGroupException : KeyNotVerifiedItemGroupException()
        class KeyNotVerifiedCollectionException : KeyNotVerifiedItemGroupException()
    }

    sealed class KeyNotVerifiedUserGroupException : KeyNotVerifiedException() {
        class KeyNotVerifiedUserException : KeyNotVerifiedUserGroupException()
    }

    sealed class KeyNotVerifiedCollectionException : KeyNotVerifiedException() {
        class KeyNotVerifiedUserException : KeyNotVerifiedCollectionException()
        class KeyNotVerifiedUserGroupException : KeyNotVerifiedCollectionException()
    }
}

class ProposeSignatureInvalidException : Exception()