package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Product(
    var id: String = UUID.randomUUID().toString(),
    var sellerId: String = "",
    var name: String,
    var description: String,
    var price: Double,
    var stock: Int,
    var category: String,
    var image: String? = null,
    var barcode: String? = null,
    
    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
