package com.androiddev

import com.androiddev.data.auth.AppJWT
import com.androiddev.data.checkIfUserExists
import com.androiddev.data.checkPasswordForEmail
import com.androiddev.data.routes.registerRoute
import com.androiddev.routes.loginRoute
import com.androiddev.routes.noteRoutes
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.jackson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    initWithSecret()
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(Authentication) {
        //configureAuth()
        jwt {
            verifier(AppJWT.instance.verifier)
            validate {
                val claim = it.payload.getClaim(AppJWT.ClAIM).asString()
                if(checkIfUserExists(claim))
                    UserIdPrincipal(claim)
                else
                    null
            }
        }
    }
    install(Routing) {
        registerRoute()
        loginRoute()
        noteRoutes()
    }

}

fun initWithSecret() {
    AppJWT.initialize("mysecret")
}

fun Authentication.Configuration.configureAuth() {
    basic() {
        realm = "Note Server"
        validate { credentials ->
            val email = credentials.name
            val password = credentials.password
            if(checkPasswordForEmail(email, password)) {
                UserIdPrincipal(email)
            } else null
        }
    }
}

