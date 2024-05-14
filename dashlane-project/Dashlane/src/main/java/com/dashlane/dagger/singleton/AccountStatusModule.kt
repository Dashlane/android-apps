package com.dashlane.dagger.singleton

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.AccountStatusPostUpdateManager
import com.dashlane.accountstatus.AccountStatusPostUpdateManagerImpl
import com.dashlane.accountstatus.AccountStatusProvider
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.AccountStatusRepositoryImpl
import com.dashlane.accountstatus.AccountStatusService
import com.dashlane.accountstatus.AccountStatusServiceImpl
import com.dashlane.accountstatus.AccountStatusStorage
import com.dashlane.accountstatus.AccountStatusStorageImpl
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepositoryImpl
import com.dashlane.accountstatus.subscriptioncode.service.SubscriptionCodeService
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.session.BySessionRepository
import com.dashlane.util.inject.OptionalProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AccountStatusModule {

    @Binds
    fun bindAccountStatusStorage(impl: AccountStatusStorageImpl): AccountStatusStorage

    @Binds
    fun bindAccountStatusService(impl: AccountStatusServiceImpl): AccountStatusService

    @Binds
    @Singleton
    fun bindAccountStatusRepository(impl: AccountStatusRepositoryImpl): AccountStatusRepository

    @Binds
    fun bindBySessionAccountStatus(impl: AccountStatusRepositoryImpl): BySessionRepository<AccountStatus>

    @Binds
    fun bindAccountStatusProvider(impl: AccountStatusProvider): OptionalProvider<AccountStatus>

    @Binds
    fun bindAccountStatusPostUpdateManager(impl: AccountStatusPostUpdateManagerImpl): AccountStatusPostUpdateManager

    @Binds
    fun bindSubscriptionCodeRepository(impl: SubscriptionCodeRepositoryImpl): SubscriptionCodeRepository

    companion object {

        @Provides
        fun provide(@LegacyWebservicesApi retrofit: Retrofit): SubscriptionCodeService {
            return retrofit.create(SubscriptionCodeService::class.java)
        }
    }
}