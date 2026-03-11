package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Cart
import org.delcom.repositories.ICartRepository
import java.util.*

class CartService(
    private val cartRepository: ICartRepository
) {
    suspend fun getCartByUser(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val carts = cartRepository.getByUser(userId)
        call.respond(DataResponse("success", "Keranjang belanja", carts))
    }

    suspend fun addToCart(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        // Mengambil userId dari claim 'userId' sesuai dengan yang ada di AuthService
        val userId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val request = call.receive<Cart>()
        // Memastikan userId diisi dari token, bukan dari body request
        request.userId = userId
        
        val cartId = cartRepository.addToCart(request)
        call.respond(DataResponse("success", "Berhasil menambah ke keranjang", cartId))
    }

    suspend fun updateQuantity(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID keranjang diperlukan")
        val request = call.receive<Map<String, Int>>()
        val quantity = request["quantity"] ?: throw AppException(400, "Quantity diperlukan")
        
        val success = cartRepository.updateQuantity(id, quantity)
        if (success) {
            call.respond(DataResponse("success", "Berhasil memperbarui kuantitas", null))
        } else {
            throw AppException(404, "Item keranjang tidak ditemukan")
        }
    }

    suspend fun removeFromCart(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID keranjang diperlukan")
        val success = cartRepository.removeFromCart(id)
        if (success) {
            call.respond(DataResponse("success", "Berhasil menghapus dari keranjang", null))
        } else {
            throw AppException(404, "Item keranjang tidak ditemukan")
        }
    }
}
