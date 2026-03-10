package org.delcom

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.configureDatabases
import org.delcom.module.appModule
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    val dotenv = try {
        dotenv {
            directory = "."
            ignoreIfMissing = true
        }
    } catch (e: Exception) {
        null
    }

    dotenv?.entries()?.forEach {
        System.setProperty(it.key, it.value)
    }

    // Set default values if not provided by .env or system environment
    val defaults = mapOf(
        "APP_HOST" to "0.0.0.0",
        "APP_PORT" to "8000",
        "DB_HOST" to "localhost",
        "DB_PORT" to "5432",
        "DB_NAME" to "db_pam_p5",
        "DB_USER" to "postgres",
        "DB_PASSWORD" to "postgres",
        "JWT_SECRET" to "default_secret_key"
    )

    defaults.forEach { (key, value) ->
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value)
        }
    }

    EngineMain.main(args)
}

fun Application.module() {

    val jwtSecret = environment.config.propertyOrNull("ktor.jwt.secret")?.getString() ?: "default-secret-key-change-me"

    install(Authentication) {
        jwt(JWTConstants.NAME) {
            realm = JWTConstants.REALM

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWTConstants.ISSUER)
                    .withAudience(JWTConstants.AUDIENCE)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload
                    .getClaim("userId")
                    .asString()

                if (!userId.isNullOrBlank())
                    JWTPrincipal(credential.payload)
                else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "status" to "error",
                        "message" to "Token tidak valid"
                    )
                )
            }
        }
    }

    install(CORS) {
        anyHost()
    }

    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls = false
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(Koin) {
        modules(appModule(jwtSecret))
    }

    configureDatabases()
    configureRouting()
}
