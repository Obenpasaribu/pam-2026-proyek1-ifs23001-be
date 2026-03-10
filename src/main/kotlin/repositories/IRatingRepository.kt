package org.delcom.repositories

import org.delcom.entities.Rating

interface IRatingRepository {
    suspend fun getByProduct(productId: String): List<Rating>
    suspend fun create(rating: Rating): String
}
