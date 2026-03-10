package org.delcom.repositories

import org.delcom.entities.Transaction

interface ITransactionRepository {
    suspend fun getByBuyer(buyerId: String): List<Transaction>
    suspend fun getBySeller(sellerId: String): List<Transaction>
    suspend fun create(transaction: Transaction): String
    suspend fun getIncomeBySeller(sellerId: String): List<Transaction>
}
