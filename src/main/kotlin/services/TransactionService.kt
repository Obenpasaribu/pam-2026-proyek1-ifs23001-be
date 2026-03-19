package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Transaction
import org.delcom.helpers.ServiceHelper
import org.delcom.repositories.*
import java.util.*

@Serializable
data class DepositRequest(
    val amount: Double
)

class TransactionService(
    private val transactionRepository: ITransactionRepository,
    private val productRepository: IProductRepository,
    private val userRepository: IUserRepository,
    private val cartRepository: ICartRepository
) {
    suspend fun checkout(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        
        val request = call.receive<Transaction>()
        request.buyerId = user.id
        
        val product = productRepository.getById(request.productId) ?: throw AppException(404, "Produk tidak ditemukan")
        if (product.stock < request.quantity) {
            throw AppException(400, "Stok tidak mencukupi")
        }
        
        val totalPayment = product.price * request.quantity
        
        if (user.balance < totalPayment) {
            throw AppException(400, "Saldo tidak mencukupi. Silakan top up.")
        }

        // 1. Potong Saldo Buyer
        user.balance -= totalPayment
        userRepository.update(user.id, user)

        // 2. Tambah Saldo Seller (Gunakan sellerBalance secara eksplisit)
        val seller = userRepository.getById(product.sellerId)
        if (seller != null) {
            seller.sellerBalance += totalPayment
            userRepository.update(seller.id, seller)
        }
        
        // 3. Update stok produk
        product.stock -= request.quantity
        productRepository.update(product.id, product)
        
        // 4. Simpan transaksi
        request.sellerId = product.sellerId
        request.totalPrice = totalPayment
        request.status = "SUCCESS"
        
        val transactionId = transactionRepository.create(request)
        call.respond(DataResponse("success", "Pembayaran Berhasil! Saldo Seller telah bertambah.", mapOf("id" to transactionId)))
    }

    suspend fun bulkCheckout(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        
        val cartItems = cartRepository.getByUser(user.id)
        if (cartItems.isEmpty()) {
            throw AppException(400, "Keranjang Anda kosong")
        }

        var totalPayment = 0.0
        val itemsToProcess = mutableListOf<Triple<org.delcom.entities.Product, Int, String>>()

        for (item in cartItems) {
            val product = productRepository.getById(item.productId) 
                ?: throw AppException(404, "Produk tidak ditemukan")
            
            if (product.stock < item.quantity) {
                throw AppException(400, "Stok produk '${product.name}' tidak mencukupi")
            }
            
            totalPayment += product.price * item.quantity
            itemsToProcess.add(Triple(product, item.quantity, item.id))
        }

        if (user.balance < totalPayment) {
            throw AppException(400, "Saldo tidak mencukupi. Total: Rp $totalPayment")
        }

        user.balance -= totalPayment
        userRepository.update(user.id, user)

        for (item in itemsToProcess) {
            val product = item.first
            val quantity = item.second
            val cartId = item.third
            val amount = product.price * quantity

            val seller = userRepository.getById(product.sellerId)
            if (seller != null) {
                seller.sellerBalance += amount
                userRepository.update(seller.id, seller)
            }

            product.stock -= quantity
            productRepository.update(product.id, product)

            transactionRepository.create(Transaction(
                buyerId = user.id,
                sellerId = product.sellerId,
                productId = product.id,
                quantity = quantity,
                totalPrice = amount,
                status = "SUCCESS"
            ))

            cartRepository.removeFromCart(cartId)
        }

        call.respond(DataResponse("success", "Checkout Keranjang Berhasil!", "Total: Rp $totalPayment"))
    }

    suspend fun deposit(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        val request = try {
            call.receive<DepositRequest>()
        } catch (e: Exception) {
            throw AppException(400, "Format jumlah deposit tidak valid")
        }
        
        val amount = request.amount
        if (amount <= 0) throw AppException(400, "Jumlah deposit harus lebih dari 0")
        
        user.balance += amount
        userRepository.update(user.id, user)
        
        call.respond(DataResponse("success", "Berhasil melakukan deposit", user.balance))
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
        val totalIncome = transactions.sumOf { it.totalPrice }
        
        call.respond(DataResponse("success", "Info pemasukan penjual", mapOf(
            "totalIncome" to totalIncome
        )))
    }
}
