package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Product
import org.delcom.repositories.IProductRepository
import java.util.*

class ProductService(
    private val productRepository: IProductRepository
) {
    suspend fun getAllProducts(call: ApplicationCall) {
        val products = productRepository.getAll()
        call.respond(DataResponse("success", "Berhasil mengambil produk", products))
    }

    suspend fun getProductById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val product = productRepository.getById(id) ?: throw AppException(404, "Produk tidak ditemukan")
        call.respond(DataResponse("success", "Berhasil mengambil produk", product))
    }

    suspend fun searchProducts(call: ApplicationCall) {
        val query = call.request.queryParameters["q"] ?: ""
        val products = productRepository.search(query)
        call.respond(DataResponse("success", "Hasil pencarian", products))
    }

    suspend fun scanBarcode(call: ApplicationCall) {
        val barcode = call.request.queryParameters["barcode"] ?: throw AppException(400, "Barcode diperlukan")
        val product = productRepository.getByBarcode(barcode) ?: throw AppException(404, "Produk tidak ditemukan")
        call.respond(DataResponse("success", "Produk ditemukan", product))
    }

    suspend fun getSellerProducts(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val products = productRepository.getBySeller(sellerId)
        call.respond(DataResponse("success", "Produk penjual", products))
    }

    suspend fun createProduct(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val request = call.receive<Product>()
        request.sellerId = sellerId
        
        val productId = productRepository.create(request)
        call.respond(DataResponse("success", "Berhasil menambah produk", mapOf("id" to productId)))
    }

    suspend fun updateProduct(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val request = call.receive<Product>()
        
        val success = productRepository.update(id, request)
        if (success) {
            call.respond(DataResponse("success", "Berhasil memperbarui produk", null))
        } else {
            throw AppException(404, "Produk tidak ditemukan")
        }
    }

    suspend fun deleteProduct(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val success = productRepository.delete(id)
        if (success) {
            call.respond(DataResponse("success", "Berhasil menghapus produk", null))
        } else {
            throw AppException(404, "Produk tidak ditemukan")
        }
    }
}
