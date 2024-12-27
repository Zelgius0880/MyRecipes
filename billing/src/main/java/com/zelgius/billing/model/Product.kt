package com.zelgius.billing.model

data class Product (
    val formattedPrice: String,
    val type: Type = Type.Subscription,
) {
    enum class Type {
        Subscription, OneTime,
    }
}