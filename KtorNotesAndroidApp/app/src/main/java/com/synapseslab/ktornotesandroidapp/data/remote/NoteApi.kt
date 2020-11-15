package com.synapseslab.ktornotesandroidapp.data.remote

import com.synapseslab.ktornotesandroidapp.data.local.entities.Note
import com.synapseslab.ktornotesandroidapp.data.remote.requests.AccountRequest
import com.synapseslab.ktornotesandroidapp.data.remote.requests.AddOwnerRequest
import com.synapseslab.ktornotesandroidapp.data.remote.responses.SimpleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NoteApi {

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/note")
    suspend fun addNote(
        @Body note: Note
    ): Response<ResponseBody>

    @DELETE("/note/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<ResponseBody>

    @POST("/add-owner")
    suspend fun addOwnerToNote(
        @Body addOwnerRequest: AddOwnerRequest
    ): Response<SimpleResponse>

    @GET("/notes")
    suspend fun getNotes(): Response<List<Note>>
}