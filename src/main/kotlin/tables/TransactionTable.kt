package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TransactionTable : UUIDTable("transactions") {
    val buyerId = uuid("buyer_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val sellerId = uuid("seller_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = uuid("product_id").references(ProductTable.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val totalPrice = double("total_price")
    val createdAt = timestamp("created_at")
}
