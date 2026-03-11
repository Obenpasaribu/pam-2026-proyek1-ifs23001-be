package org.delcom.repositories

import org.delcom.dao.CartDAO
import org.delcom.entities.Cart
import org.delcom.helpers.cartDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.CartTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

class CartRepository : ICartRepository {
    override suspend fun getByUser(userId: String): List<Cart> = suspendTransaction {
        try {
            CartDAO.find { CartTable.userId eq UUID.fromString(userId) }
                .map { cartDAOToModel(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addToCart(cart: Cart): String = suspendTransaction {
        CartDAO.new {
            userId = UUID.fromString(cart.userId)
            productId = UUID.fromString(cart.productId)
            quantity = cart.quantity
            createdAt = cart.createdAt
            updatedAt = cart.updatedAt
        }.id.value.toString()
    }

    override suspend fun updateQuantity(id: String, quantity: Int): Boolean = suspendTransaction {
        val dao = CartDAO.findById(UUID.fromString(id)) ?: return@suspendTransaction false
        dao.quantity = quantity
        dao.updatedAt = kotlinx.datetime.Clock.System.now()
        true
    }

    override suspend fun removeFromCart(id: String): Boolean = suspendTransaction {
        CartTable.deleteWhere { CartTable.id eq UUID.fromString(id) } > 0
    }

    override suspend fun clearCart(userId: String): Boolean = suspendTransaction {
        CartTable.deleteWhere { CartTable.userId eq UUID.fromString(userId) } > 0
    }
}
