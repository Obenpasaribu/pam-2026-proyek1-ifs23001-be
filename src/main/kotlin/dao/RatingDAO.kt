package org.delcom.dao

import org.delcom.tables.RatingTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class RatingDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, RatingDAO>(RatingTable)

    var productId by RatingTable.productId
    var userId by RatingTable.userId
    var score by RatingTable.score
    var comment by RatingTable.comment
    var createdAt by RatingTable.createdAt
}
