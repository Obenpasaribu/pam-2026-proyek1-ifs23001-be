package org.delcom.dao

import org.delcom.tables.ProductTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class ProductDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, ProductDAO>(ProductTable)

    var sellerId by ProductTable.sellerId
    var name by ProductTable.name
    var description by ProductTable.description
    var price by ProductTable.price
    var stock by ProductTable.stock
    var category by ProductTable.category
    var image by ProductTable.image
    var barcode by ProductTable.barcode
    var createdAt by ProductTable.createdAt
    var updatedAt by ProductTable.updatedAt
}
