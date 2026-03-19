package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.*
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {
    // User Repository
    single<IUserRepository> {
        UserRepository()
    }

    // User Service
    single {
        UserService(get(),get())
    }

    // Refresh Token Repository
    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }

    // Auth Service
    single {
        AuthService(jwtSecret,get(), get())
    }

    // Product
    single<IProductRepository> {
        ProductRepository()
    }
    single {
        ProductService(get())
    }

    // Cart
    single<ICartRepository> {
        CartRepository()
    }
    single {
        CartService(get())
    }

    // Rating
    single<IRatingRepository> {
        RatingRepository()
    }

    // Transaction
    single<ITransactionRepository> {
        TransactionRepository()
    }
    single {
        TransactionService(get(), get(), get())
    }

    // Todo (Keeping for compatibility or can be removed)
    single<ITodoRepository> {
        TodoRepository()
    }
    single {
        TodoService(get(),get())
    }
}
