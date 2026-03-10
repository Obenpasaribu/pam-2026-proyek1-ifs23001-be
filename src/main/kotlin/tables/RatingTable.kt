package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RatingTable : UUIDTable("ratings") {
    val productId = uuid("product_id").references(ProductTable.id, onDelete = ReferenceOption.CASCADE)
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val score = integer("score")
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at")
}
