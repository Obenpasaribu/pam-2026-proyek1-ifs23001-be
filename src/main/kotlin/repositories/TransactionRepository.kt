package org.delcom.repositories

import org.delcom.dao.TransactionDAO
import org.delcom.entities.Transaction
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.transactionDAOToModel
import org.delcom.tables.TransactionTable
import java.util.*

class TransactionRepository : ITransactionRepository {
    override suspend fun getByBuyer(buyerId: String): List<Transaction> = suspendTransaction {
        TransactionDAO.find { TransactionTable.buyerId eq UUID.fromString(buyerId) }.map(::transactionDAOToModel)
    }

    override suspend fun getBySeller(sellerId: String): List<Transaction> = suspendTransaction {
        TransactionDAO.find { TransactionTable.sellerId eq UUID.fromString(sellerId) }.map(::transactionDAOToModel)
    }

    override suspend fun create(transaction: Transaction): String = suspendTransaction {
        TransactionDAO.new {
            buyerId = UUID.fromString(transaction.buyerId)
            sellerId = UUID.fromString(transaction.sellerId)
            productId = UUID.fromString(transaction.productId)
            quantity = transaction.quantity
            totalPrice = transaction.totalPrice
            createdAt = transaction.createdAt
        }.id.value.toString()
    }

    override suspend fun getIncomeBySeller(sellerId: String): List<Transaction> = suspendTransaction {
        TransactionDAO.find { TransactionTable.sellerId eq UUID.fromString(sellerId) }
            .sortedBy { it.createdAt }
            .map(::transactionDAOToModel)
    }
}
