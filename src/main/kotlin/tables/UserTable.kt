package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : UUIDTable("users") {
    val name = varchar("name", 100)
    val username = varchar("username", 50)
    val password = varchar("password", 255)
    val role = varchar("role", 20).default("BUYER")
    val photo = varchar("photo", 255).nullable()
    val bio = text("bio").nullable()
    val balance = double("balance").default(0.0)
    val sellerBalance = double("seller_balance").default(0.0)
    val walletCode = varchar("wallet_code", 10).nullable().uniqueIndex()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
