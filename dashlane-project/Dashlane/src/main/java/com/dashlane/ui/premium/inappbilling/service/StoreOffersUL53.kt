package com.dashlane.ui.premium.inappbilling.service

import com.dashlane.device.DeviceInfoRepository
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.session.Session
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.useractivity.log.usage.UsageLogCode53
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithPremiumStatus

class StoreOffersUL53(
    private val session: Session,
    private val accountStatusRepository: AccountStatusRepository,
    private val usageLogRepository: UsageLogRepository,
    private val deviceInfoRepository: DeviceInfoRepository
) {

    fun send(storeOffers: StoreOffersService.Data) {
        val usageLogCode53 = UsageLogCode53(
            availablePlans = getAvailablePlans(storeOffers),
            country = deviceInfoRepository.deviceCountry
        )
        send(usageLogCode53.copyWithPremiumStatus(accountStatusRepository.getPremiumStatus(session)))
    }

    private fun send(usageLogCode53: UsageLogCode53) {
        usageLogRepository.enqueue(usageLogCode53)
    }

    

    private fun getAvailablePlans(storeOffers: StoreOffersService.Data): String? {
        return sequence {
            yieldAll(storeOffers.essentials.products.map { it.sku })
            yieldAll(storeOffers.premium.products.map { it.sku })
            yieldAll(storeOffers.family.products.map { it.sku })
        }
            .joinToString(",")
            .takeUnless(String::isEmpty)
    }
}