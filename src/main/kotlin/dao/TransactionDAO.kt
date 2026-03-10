package org.delcom.dao

import org.delcom.tables.TransactionTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class TransactionDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, TransactionDAO>(TransactionTable)

    var buyerId by TransactionTable.buyerId
    var sellerId by TransactionTable.sellerId
    var productId by TransactionTable.productId
    var quantity by TransactionTable.quantity
    var totalPrice by TransactionTable.totalPrice
    var createdAt by TransactionTable.createdAt
}
