package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.*
import org.delcom.entities.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    id = dao.id.value.toString(),
    name = dao.name,
    username = dao.username,
    password = dao.password,
    role = UserRole.valueOf(dao.role),
    photo = dao.photo,
    bio = dao.bio,
    balance = dao.balance,
    sellerBalance = dao.sellerBalance,
    walletCode = dao.walletCode,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun productDAOToModel(dao: ProductDAO) = Product(
    id = dao.id.value.toString(),
    sellerId = dao.sellerId.toString(),
    name = dao.name,
    description = dao.description,
    price = dao.price,
    stock = dao.stock,
    category = dao.category,
    image = dao.image,
    barcode = dao.barcode,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun cartDAOToModel(dao: CartDAO): Cart {
    val cart = Cart(
        id = dao.id.value.toString(),
        userId = dao.userId.toString(),
        productId = dao.productId.toString(),
        quantity = dao.quantity,
        createdAt = dao.createdAt,
        updatedAt = dao.updatedAt
    )
    
    // Ambil data produk terkait
    try {
        val productDao = ProductDAO.findById(dao.productId)
        if (productDao != null) {
            cart.product = productDAOToModel(productDao)
        }
    } catch (e: Exception) {
        println("Error mapping product in cart: ${e.message}")
    }
    
    return cart
}

fun ratingDAOToModel(dao: RatingDAO) = Rating(
    id = dao.id.value.toString(),
    productId = dao.productId.toString(),
    userId = dao.userId.toString(),
    score = dao.score,
    comment = dao.comment,
    createdAt = dao.createdAt
)

fun transactionDAOToModel(dao: TransactionDAO) = Transaction(
    id = dao.id.value.toString(),
    buyerId = dao.buyerId.toString(),
    sellerId = dao.sellerId.toString(),
    productId = dao.productId.toString(),
    quantity = dao.quantity,
    totalPrice = dao.totalPrice,
    status = dao.status,
    createdAt = dao.createdAt
)

fun todoDAOToModel(dao: TodoDAO) = Todo(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    title = dao.title,
    description = dao.description,
    isDone = dao.isDone,
    urgency = dao.urgency,
    cover = dao.cover,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)
