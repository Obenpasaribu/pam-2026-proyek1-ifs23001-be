package org.delcom.repositories

import org.delcom.dao.RatingDAO
import org.delcom.entities.Rating
import org.delcom.helpers.ratingDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.RatingTable
import java.util.*

class RatingRepository : IRatingRepository {
    override suspend fun getByProduct(productId: String): List<Rating> = suspendTransaction {
        RatingDAO.find { RatingTable.productId eq UUID.fromString(productId) }.map(::ratingDAOToModel)
    }

    override suspend fun create(rating: Rating): String = suspendTransaction {
        RatingDAO.new {
            productId = UUID.fromString(rating.productId)
            userId = UUID.fromString(rating.userId)
            score = rating.score
            comment = rating.comment
            createdAt = rating.createdAt
        }.id.value.toString()
    }
}
