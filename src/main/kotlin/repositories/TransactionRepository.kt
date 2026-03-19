package org.delcom.repositories

import org.delcom.dao.TransactionDAO
import org.delcom.entities.Transaction
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.transactionDAOToModel
import org.delcom.tables.TransactionTable
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.sum
import java.util.*

class TransactionRepository : ITransactionRepository {
    override suspend fun getByBuyer(buyerId: String): List<Transaction> = suspendTransaction {
        TransactionDAO.find { TransactionTable.buyerId eq UUID.fromString(buyerId) }
            .map { transactionDAOToModel(it) }
    }

    override suspend fun getIncomeBySeller(sellerId: String): List<Transaction> = suspendTransaction {
        TransactionDAO.find { TransactionTable.sellerId eq UUID.fromString(sellerId) }
            .map { transactionDAOToModel(it) }
    }

    override suspend fun create(transaction: Transaction): String = suspendTransaction {
        TransactionDAO.new {
            buyerId = UUID.fromString(transaction.buyerId)
            sellerId = UUID.fromString(transaction.sellerId)
            productId = UUID.fromString(transaction.productId)
            quantity = transaction.quantity
            totalPrice = transaction.totalPrice
            status = transaction.status
            createdAt = transaction.createdAt
        }.id.value.toString()
    }
}
