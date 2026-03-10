package org.delcom.repositories

import org.delcom.entities.Cart

interface ICartRepository {
    suspend fun getByUser(userId: String): List<Cart>
    suspend fun addToCart(cart: Cart): String
    suspend fun updateQuantity(id: String, quantity: Int): Boolean
    suspend fun removeFromCart(id: String): Boolean
    suspend fun clearCart(userId: String): Boolean
}
