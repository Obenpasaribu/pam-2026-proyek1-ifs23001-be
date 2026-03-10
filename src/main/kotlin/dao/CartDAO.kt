package org.delcom.dao

import org.delcom.tables.CartTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class CartDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, CartDAO>(CartTable)

    var userId by CartTable.userId
    var productId by CartTable.productId
    var quantity by CartTable.quantity
    var createdAt by CartTable.createdAt
    var updatedAt by CartTable.updatedAt
}
