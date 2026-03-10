package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Transaction
import org.delcom.repositories.ITransactionRepository
import org.delcom.repositories.IProductRepository
import java.util.*

class TransactionService(
    private val transactionRepository: ITransactionRepository,
    private val productRepository: IProductRepository
) {
    suspend fun checkout(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val buyerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val request = call.receive<Transaction>()
        request.buyerId = buyerId
        
        // Periksa produk dan stok
        val product = productRepository.getById(request.productId) ?: throw AppException(404, "Produk tidak ditemukan")
        if (product.stock < request.quantity) {
            throw AppException(400, "Stok tidak mencukupi")
        }
        
        // Update stok produk
        product.stock -= request.quantity
        productRepository.update(product.id, product)
        
        // Simpan transaksi
        request.sellerId = product.sellerId
        request.totalPrice = product.price * request.quantity
        
        val transactionId = transactionRepository.create(request)
        call.respond(DataResponse("success", "Pembayaran berhasil", mapOf("id" to transactionId)))
    }

    suspend fun getBuyerTransactions(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val buyerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val transactions = transactionRepository.getByBuyer(buyerId)
        call.respond(DataResponse("success", "Riwayat transaksi pembeli", transactions))
    }

    suspend fun getSellerIncome(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val transactions = transactionRepository.getIncomeBySeller(sellerId)
        
        // Data untuk grafik (disederhanakan: list total harga dan tanggal)
        val chartData = transactions.map { 
            mapOf("date" to it.createdAt.toString(), "amount" to it.totalPrice)
        }
        
        val totalIncome = transactions.sumOf { it.totalPrice }
        
        call.respond(DataResponse("success", "Info pemasukan penjual", mapOf(
            "totalIncome" to totalIncome,
            "chartData" to chartData
        )))
    }
}
