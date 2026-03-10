package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Rating(
    var id: String = UUID.randomUUID().toString(),
    var productId: String,
    var userId: String,
    var score: Int,
    var comment: String? = null,
    
    @Contextual
    val createdAt: Instant = Clock.System.now(),
)
