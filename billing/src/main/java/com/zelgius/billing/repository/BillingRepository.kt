package com.zelgius.billing.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.zelgius.billing.BuildConfig
import com.zelgius.billing.model.Product
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import zelgius.com.myrecipes.data.logger.Logger
import zelgius.com.myrecipes.data.repository.RemoteConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteConfigRepository: RemoteConfigRepository,
) {
    private val repositoryJob = SupervisorJob() // Use SupervisorJob
    private val coroutineCope = CoroutineScope(Dispatchers.IO + repositoryJob)

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }

    private var billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .setListener(purchasesUpdatedListener)
        .build()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    suspend fun start() {
        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTED || !BuildConfig.BILLING_ENABLED) return

        suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        continuation.resumeWith(Result.success(Unit))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    continuation.resumeWith(Result.success(Unit))
                }
            })
        }
    }

    suspend fun checkPurchase() {
        if(!BuildConfig.BILLING_ENABLED) {
            _isPremium.value = true
            return
        }

        start()
        val oneTime = checkPurchase(Product.Type.OneTime)
        if (!oneTime) {
            val subscription = checkPurchase(Product.Type.Subscription)
            _isPremium.value = subscription
        } else {
            _isPremium.value = true
        }
    }

    private suspend fun checkPurchase(product: Product.Type) =
        suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(
                when (product) {
                    Product.Type.OneTime -> QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.INAPP)
                        .build()

                    Product.Type.Subscription -> QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.SUBS)
                        .build()
                }
            ) { result, purchases ->
                continuation.resumeWith(Result.success(purchases.isNotEmpty() && purchases.any { it.purchaseState == PURCHASED }))
            }
        }

    suspend fun fetchProducts() {
        start()
        _products.value = queryProducts(Product.Type.Subscription).map {
            val offer = it.subscriptionOfferDetails?.first()
            val phase = offer?.pricingPhases?.pricingPhaseList?.first()

            Product(formattedPrice = phase?.formattedPrice ?: "", type = Product.Type.Subscription)
        }
    }

    private suspend fun queryProducts(product: Product.Type): List<ProductDetails> {
        start()
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    getProductQuery(product)
                )
                .build()

        val result = billingClient.queryProductDetails(queryProductDetailsParams)
        return result.productDetailsList ?: emptyList()
    }

    private suspend fun getProductQuery(product: Product.Type) = when (product) {
        Product.Type.Subscription -> listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(remoteConfigRepository.billingSubscriptionKey.first())
                .setProductType(ProductType.SUBS)
                .build()
        )

        Product.Type.OneTime -> listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(remoteConfigRepository.billingProductKey.first())
                .setProductType(ProductType.INAPP)
                .build()
        )
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                    if (it.responseCode == BillingResponseCode.OK) Logger.i("Purchase ok")
                    else Logger.e("Purchase failed: ${it.debugMessage}")

                    coroutineCope.launch {
                        checkPurchase()
                    }
                }
            }

        }
    }


    suspend fun startPurchase(activity: Activity, product: Product.Type) {
        val productDetails = queryProducts(product)

        val productDetail = when (product) {
            Product.Type.OneTime -> productDetails.first { it.productType == ProductType.INAPP }
            Product.Type.Subscription -> productDetails.first { it.productType == ProductType.SUBS }
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetail).let {
                    if (product == Product.Type.Subscription) it.setOfferToken(
                        productDetail.subscriptionOfferDetails?.first()?.offerToken ?: ""
                    )
                    else it
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}