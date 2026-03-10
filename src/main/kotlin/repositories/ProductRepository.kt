package org.delcom.repositories

import org.delcom.dao.ProductDAO
import org.delcom.entities.Product
import org.delcom.helpers.productDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ProductTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.util.*

class ProductRepository : IProductRepository {
    override suspend fun getAll(): List<Product> = suspendTransaction {
        ProductDAO.all().map(::productDAOToModel)
    }

    override suspend fun getById(id: String): Product? = suspendTransaction {
        ProductDAO.findById(UUID.fromString(id))?.let(::productDAOToModel)
    }

    override suspend fun getByBarcode(barcode: String): Product? = suspendTransaction {
        ProductDAO.find { ProductTable.barcode eq barcode }.firstOrNull()?.let(::productDAOToModel)
    }

    override suspend fun search(query: String): List<Product> = suspendTransaction {
        ProductDAO.find {
            (ProductTable.name.lowerCase() like "%${query.lowercase()}%") or
            (ProductTable.description.lowerCase() like "%${query.lowercase()}%") or
            (ProductTable.category.lowerCase() like "%${query.lowercase()}%")
        }.map(::productDAOToModel)
    }

    override suspend fun getBySeller(sellerId: String): List<Product> = suspendTransaction {
        ProductDAO.find { ProductTable.sellerId eq UUID.fromString(sellerId) }.map(::productDAOToModel)
    }

    override suspend fun create(product: Product): String = suspendTransaction {
        ProductDAO.new {
            sellerId = UUID.fromString(product.sellerId)
            name = product.name
            description = product.description
            price = product.price
            stock = product.stock
            category = product.category
            image = product.image
            barcode = product.barcode
            createdAt = product.createdAt
            updatedAt = product.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(id: String, product: Product): Boolean = suspendTransaction {
        val dao = ProductDAO.findById(UUID.fromString(id)) ?: return@suspendTransaction false
        dao.name = product.name
        dao.description = product.description
        dao.price = product.price
        dao.stock = product.stock
        dao.category = product.category
        dao.image = product.image
        dao.barcode = product.barcode
        dao.updatedAt = product.updatedAt
        true
    }

    override suspend fun delete(id: String): Boolean = suspendTransaction {
        ProductTable.deleteWhere { ProductTable.id eq UUID.fromString(id) } > 0
    }
}
