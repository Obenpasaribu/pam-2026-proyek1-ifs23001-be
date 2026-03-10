package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ProductTable : UUIDTable("products") {
    val sellerId = uuid("seller_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = text("description")
    val price = double("price")
    val stock = integer("stock")
    val category = varchar("category", 100)
    val image = varchar("image", 255).nullable()
    val barcode = varchar("barcode", 100).nullable().uniqueIndex()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
