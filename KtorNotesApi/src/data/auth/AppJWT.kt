package com.androiddev.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

open class AppJWT private constructor(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    /**
     * Generates JWT token from [userId].
     */
    fun sign(userId: String): String = JWT
        .create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(ClAIM, userId)
        //.withExpiresAt(expiresAt())
        .sign(algorithm)

    private fun expiresAt() =
        Date(System.currentTimeMillis() + 3_600_000 * 24) // 24 hours

    companion object {
        lateinit var instance: AppJWT
            private set

        fun initialize(secret: String) {
            synchronized(this) {
                if (!this::instance.isInitialized) {
                    instance = AppJWT(secret)
                }
            }
        }

        private const val ISSUER = "APP-JWT-Issuer"
        private const val AUDIENCE = "https://app-api.herokuapp.com"
        const val ClAIM = "userId"
    }
}