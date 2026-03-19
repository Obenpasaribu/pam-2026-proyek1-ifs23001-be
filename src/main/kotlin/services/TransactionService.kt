package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Transaction
import org.delcom.helpers.ServiceHelper
import org.delcom.repositories.ITransactionRepository
import org.delcom.repositories.IProductRepository
import org.delcom.repositories.IUserRepository
import java.util.*

class TransactionService(
    private val transactionRepository: ITransactionRepository,
    private val productRepository: IProductRepository,
    private val userRepository: IUserRepository
) {
    suspend fun checkout(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        
        val request = call.receive<Transaction>()
        request.buyerId = user.id
        
        // Periksa produk dan stok
        val product = productRepository.getById(request.productId) ?: throw AppException(404, "Produk tidak ditemukan")
        if (product.stock < request.quantity) {
            throw AppException(400, "Stok tidak mencukupi")
        }
        
        val totalPayment = product.price * request.quantity
        
        // VALIDASI WALLET & SALDO
        if (user.balance < totalPayment) {
            throw AppException(400, "Saldo tidak mencukupi. Silakan top up.")
        }

        // Potong Saldo Buyer
        user.balance -= totalPayment
        userRepository.update(user.id, user)

        // Tambah Saldo Seller
        val seller = userRepository.getById(product.sellerId)
        if (seller != null) {
            seller.balance += totalPayment
            userRepository.update(seller.id, seller)
        }
        
        // Update stok produk
        product.stock -= request.quantity
        productRepository.update(product.id, product)
        
        // Simpan transaksi
        request.sellerId = product.sellerId
        request.totalPrice = totalPayment
        
        val transactionId = transactionRepository.create(request)
        call.respond(DataResponse("success", "Pembayaran menggunakan Wallet berhasil", mapOf("id" to transactionId)))
    }

    suspend fun deposit(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        val request = call.receive<Map<String, Double>>()
        val amount = request["amount"] ?: throw AppException(400, "Jumlah deposit diperlukan")
        
        if (amount <= 0) throw AppException(400, "Jumlah harus lebih dari 0")
        
        user.balance += amount
        userRepository.update(user.id, user)
        
        call.respond(DataResponse("success", "Berhasil melakukan deposit Rp $amount", null))
    }

    suspend fun getBuyerTransactions(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val buyerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val transactions = transactionRepository.getByBuyer(buyerId)
        call.respond(DataResponse("success", "Riwayat transaksi pembeli", transactions))
    }

    suspend fun getSellerTransactions(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val transactions = transactionRepository.getIncomeBySeller(sellerId)
        call.respond(DataResponse("success", "Riwayat transaksi penjual", transactions))
    }

    suspend fun getSellerIncome(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val transactions = transactionRepository.getIncomeBySeller(sellerId)
        
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
