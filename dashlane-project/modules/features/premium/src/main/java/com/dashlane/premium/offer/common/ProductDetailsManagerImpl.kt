package com.dashlane.premium.offer.common

import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.inappbilling.withServiceConnection
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import javax.inject.Inject

class ProductDetailsManagerImpl @Inject constructor(private val billingManager: BillingManager) :
    ProductDetailsManager {

    private val productDetailsMap: MutableMap<String, ProductDetailsWrapper> = mutableMapOf()

    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    override suspend fun getProductDetailsMap(productIds: List<String>): Map<String, ProductDetailsWrapper> {
        val localResults = productIds.associateWith { productDetailsMap[it] }
            .filterValues { it != null }
            .mapValues { it.value as ProductDetailsWrapper }

        return if (localResults.size == productIds.size) {
            localResults
        } else {
            
            
            
            try {
                fetchProductDetails(productIds)
            } catch (t: ProductDetailsManager.NoProductDetailsResult) {
                mapOf()
            }
        }
    }

    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    override suspend fun getProductDetails(productId: String): ProductDetailsWrapper? {
        return productDetailsMap[productId] ?: fetchProductDetails(listOf(productId))[productId]
    }

    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    private suspend fun fetchProductDetails(productIds: List<String>): Map<String, ProductDetailsWrapper> {
        val productIdList = productIds - productDetailsMap.keys
        if (productIdList.isEmpty()) {
            return mapOf()
        }
        val serviceResult = billingManager.withServiceConnection {
            queryProductDetails(productIdList)
        }
        val loadedProductDetails = when (serviceResult) {
            is ServiceResult.Success.Products -> serviceResult.productDetailsList
            else -> throw ProductDetailsManager.NoProductDetailsResult
        }
        loadedProductDetails.mapNotNull { ProductDetailsWrapper.fromProductDetailsOrNull(it) }
            .forEach { productDetailsMap[it.productId] = it }

        return productDetailsMap.filter { it.key in productIds }
    }
}