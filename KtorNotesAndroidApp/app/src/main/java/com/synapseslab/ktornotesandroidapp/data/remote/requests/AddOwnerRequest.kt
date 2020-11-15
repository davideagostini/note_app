package com.synapseslab.ktornotesandroidapp.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val noteID: String
)