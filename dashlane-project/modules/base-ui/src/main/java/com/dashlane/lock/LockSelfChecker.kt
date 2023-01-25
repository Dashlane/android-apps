package com.dashlane.lock



interface LockSelfChecker {

    

    fun selfCheck()

    

    fun markCredentialsEmpty()
}