package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.entities.Product
import org.delcom.repositories.IProductRepository
import java.io.File
import java.util.*

class ProductService(
    private val productRepository: IProductRepository
) {
    suspend fun getAllProducts(call: ApplicationCall) {
        val products = productRepository.getAll().map { it.withImageUrl(call) }
        call.respond(DataResponse("success", "Berhasil mengambil produk", mapOf("products" to products)))
    }

    suspend fun getProductById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val product = productRepository.getById(id)?.withImageUrl(call) ?: throw AppException(404, "Produk tidak ditemukan")
        call.respond(DataResponse("success", "Berhasil mengambil produk", mapOf("product" to product)))
    }

    suspend fun searchProducts(call: ApplicationCall) {
        val query = call.request.queryParameters["q"] ?: ""
        val products = productRepository.search(query).map { it.withImageUrl(call) }
        call.respond(DataResponse("success", "Hasil pencarian", mapOf("products" to products)))
    }

    suspend fun scanBarcode(call: ApplicationCall) {
        val barcode = call.request.queryParameters["barcode"] ?: throw AppException(400, "Barcode diperlukan")
        val product = productRepository.getByBarcode(barcode)?.withImageUrl(call) ?: throw AppException(404, "Produk tidak ditemukan")
        call.respond(DataResponse("success", "Produk ditemukan", mapOf("product" to product)))
    }

    suspend fun getSellerProducts(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        val products = productRepository.getBySeller(sellerId).map { it.withImageUrl(call) }
        call.respond(DataResponse("success", "Produk penjual", mapOf("products" to products)))
    }

    suspend fun createProduct(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val sellerId = principal?.payload?.getClaim("userId")?.asString() ?: throw AppException(401, "Unauthorized")
        
        var name = ""
        var description = ""
        var price = 0.0
        var stock = 0
        var category = ""
        var barcode: String? = null
        var imagePath: String? = null

        val multipartData = call.receiveMultipart()
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toDoubleOrNull() ?: 0.0
                        "stock" -> stock = part.value.toIntOrNull() ?: 0
                        "category" -> category = part.value
                        "barcode" -> barcode = part.value
                    }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/products/$fileName"
                    
                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        imagePath = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (name.isBlank()) throw AppException(400, "Nama produk tidak boleh kosong")

        val product = Product(
            sellerId = sellerId,
            name = name,
            description = description,
            price = price,
            stock = stock,
            category = category,
            image = imagePath,
            barcode = barcode
        )
        
        val productId = productRepository.create(product)
        call.respond(DataResponse("success", "Berhasil menambah produk", mapOf("id" to productId)))
    }

    suspend fun updateProduct(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val existingProduct = productRepository.getById(id) ?: throw AppException(404, "Produk tidak ditemukan")
        
        var name = existingProduct.name
        var description = existingProduct.description
        var price = existingProduct.price
        var stock = existingProduct.stock
        var category = existingProduct.category
        var barcode = existingProduct.barcode
        var imagePath = existingProduct.image

        val multipartData = call.receiveMultipart()
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toDoubleOrNull() ?: price
                        "stock" -> stock = part.value.toIntOrNull() ?: stock
                        "category" -> category = part.value
                        "barcode" -> barcode = part.value
                    }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/products/$fileName"
                    
                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        
                        // Hapus file lama jika ada
                        imagePath?.let { 
                            val oldFile = File(it)
                            if (oldFile.exists()) oldFile.delete() 
                        }
                        imagePath = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        val updatedProduct = existingProduct.apply {
            this.name = name
            this.description = description
            this.price = price
            this.stock = stock
            this.category = category
            this.barcode = barcode
            this.image = imagePath
            this.updatedAt = kotlinx.datetime.Clock.System.now()
        }
        
        val success = productRepository.update(id, updatedProduct)
        if (success) {
            call.respond(DataResponse("success", "Berhasil memperbarui produk", null))
        } else {
            throw AppException(404, "Produk tidak ditemukan")
        }
    }

    suspend fun deleteProduct(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val product = productRepository.getById(id)
        
        val success = productRepository.delete(id)
        if (success) {
            // Hapus file image dari disk
            product?.image?.let { File(it).delete() }
            call.respond(DataResponse("success", "Berhasil menghapus produk", null))
        } else {
            throw AppException(404, "Produk tidak ditemukan")
        }
    }

    suspend fun getProductImage(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID produk diperlukan")
        val product = productRepository.getById(id) ?: throw AppException(404, "Produk tidak ditemukan")

        if (product.image == null) {
            throw AppException(404, "Produk belum memiliki gambar")
        }

        val file = File(product.image!!)
        if (!file.exists()) {
            throw AppException(404, "File gambar tidak ditemukan")
        }

        call.respondFile(file)
    }

    private fun Product.withImageUrl(call: ApplicationCall): Product {
        if (image == null) return this
        val host = call.request.host()
        val port = call.request.port()
        val scheme = call.request.local.scheme
        // Generate URL ke endpoint khusus gambar agar stabil
        val fullUrl = "$scheme://$host:$port/products/image/$id"
        return this.copy(image = fullUrl)
    }
}
