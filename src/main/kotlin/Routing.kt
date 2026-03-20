package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.*
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureRouting() {
    val authService: AuthService by inject()
    val userService: UserService by inject()
    val productService: ProductService by inject()
    val cartService: CartService by inject()
    val transactionService: TransactionService by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Unknown error",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Delcom PC Sell telah berjalan.")
        }

        route("/auth") {
            post("/login") { authService.postLogin(call) }
            post("/register") { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // Endpoint Publik untuk Gambar (Agar Coil/Glide bisa akses tanpa Token)
        get("/users/photo/{id}") { userService.getPhoto(call) }
        get("/products/image/{id}") { productService.getProductImage(call) }

        authenticate(JWTConstants.NAME) {
            route("/users") {
                get("/me") { userService.getMe(call) }
                put("/me") { userService.putMe(call) }
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo") { userService.putMyPhoto(call) }
            }

            // Fitur Wallet & Transaksi
            route("/wallet") {
                post("/deposit") { transactionService.deposit(call) }
            }

            route("/transactions") {
                post("/checkout") { transactionService.checkout(call) }
                post("/bulk-checkout") { transactionService.bulkCheckout(call) }
                get("/buyer") { transactionService.getBuyerTransactions(call) }
                get("/seller") { transactionService.getSellerTransactions(call) }
                get("/seller/income") { transactionService.getSellerIncome(call) }
            }

            // Fitur Produk (Buyer & Seller)
            route("/products") {
                get { productService.getAllProducts(call) }
                get("/search") { productService.searchProducts(call) }
                get("/scan") { productService.scanBarcode(call) }
                get("/{id}") { productService.getProductById(call) }

                // Seller Only
                post { productService.createProduct(call) }
                put("/{id}") { productService.updateProduct(call) }
                delete("/{id}") { productService.deleteProduct(call) }
                get("/seller/me") { productService.getSellerProducts(call) }
            }

            // Fitur Keranjang (Buyer)
            route("/cart") {
                get { cartService.getCartByUser(call) }
                post { cartService.addToCart(call) }
                put("/{id}") { cartService.updateQuantity(call) }
                delete("/{id}") { cartService.removeFromCart(call) }
            }
        }
    }
}
