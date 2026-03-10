package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Transaction(
    var id: String = UUID.randomUUID().toString(),
    var buyerId: String,
    var sellerId: String,
    var productId: String,
    var quantity: Int,
    var totalPrice: Double,
    
    @Contextual
    val createdAt: Instant = Clock.System.now(),
)
