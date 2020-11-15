package com.androiddev.data.responses

data class SimpleResponse(
    val successful: Boolean,
    val message: String,
    val token: String? = null
)