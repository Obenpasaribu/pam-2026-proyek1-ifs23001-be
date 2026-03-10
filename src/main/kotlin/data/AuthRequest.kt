package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.User
import org.delcom.entities.UserRole

@Serializable
data class AuthRequest(
    var name: String = "",
    var username: String = "",
    var password: String = "",
    var newPassword: String = "",
    var bio: String? = null,
    var role: String = "BUYER"
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "username" to username,
            "password" to password,
            "newPassword" to newPassword,
            "bio" to bio,
            "role" to role
        )
    }

    fun toEntity(): User {
        return User(
            name = name,
            username = username,
            password = password,
            role = try { UserRole.valueOf(role.uppercase()) } catch (e: Exception) { UserRole.BUYER },
            bio = bio,
            updatedAt = Clock.System.now()
        )
    }
}
