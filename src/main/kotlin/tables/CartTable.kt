package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CartTable : UUIDTable("carts") {
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = uuid("product_id").references(ProductTable.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
