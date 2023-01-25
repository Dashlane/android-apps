package com.dashlane.authentication

import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository



interface AuthenticationComponent {
    val emailRepository: AuthenticationEmailRepository
    val secondFactorRepository: AuthenticationSecondFactoryRepository
    val passwordRepository: AuthenticationPasswordRepository
}