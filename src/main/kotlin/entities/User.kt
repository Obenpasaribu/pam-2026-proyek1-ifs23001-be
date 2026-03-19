package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class UserRole {
    BUYER, SELLER
}

@Serializable
data class User(
    var id : String = UUID.randomUUID().toString(),
    var name: String,
    var username: String,
    var password: String,
    var role: UserRole = UserRole.BUYER,
    var photo: String? = null,
    var bio: String? = null,
    var balance: Double = 0.0,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
