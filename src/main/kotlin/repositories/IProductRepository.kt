package org.delcom.repositories

import org.delcom.entities.Product

interface IProductRepository {
    suspend fun getAll(): List<Product>
    suspend fun getById(id: String): Product?
    suspend fun getByBarcode(barcode: String): Product?
    suspend fun search(query: String): List<Product>
    suspend fun getBySeller(sellerId: String): List<Product>
    suspend fun create(product: Product): String
    suspend fun update(id: String, product: Product): Boolean
    suspend fun delete(id: String): Boolean
}
