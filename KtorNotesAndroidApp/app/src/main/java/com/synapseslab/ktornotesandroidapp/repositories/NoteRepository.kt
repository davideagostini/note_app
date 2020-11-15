package com.synapseslab.ktornotesandroidapp.repositories

import android.app.Application
import com.synapseslab.ktornotesandroidapp.data.local.NoteDao
import com.synapseslab.ktornotesandroidapp.data.local.entities.LocallyDeletedNoteID
import com.synapseslab.ktornotesandroidapp.data.local.entities.Note
import com.synapseslab.ktornotesandroidapp.data.remote.NoteApi
import com.synapseslab.ktornotesandroidapp.data.remote.requests.AccountRequest
import com.synapseslab.ktornotesandroidapp.data.remote.requests.AddOwnerRequest
import com.synapseslab.ktornotesandroidapp.other.Resource
import com.synapseslab.ktornotesandroidapp.other.checkForInternetConnection
import com.synapseslab.ktornotesandroidapp.other.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
) {

    suspend fun insertNote(note: Note) {
        val response = try {
            noteApi.addNote(note)
        } catch(e: Exception) {
            null
        }
        if(response != null && response.isSuccessful) {
            noteDao.insertNote(note.apply { isSynced = true })
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun insertNotes(notes: List<Note>) {
        notes.forEach { insertNote(it) }
    }

    suspend fun deleteNote(noteID: String) {
        val response = try {
            noteApi.deleteNote(noteID)
        } catch (e: Exception) {
            null
        }
        noteDao.deleteNoteById(noteID)

        if (response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeletedNoteId(LocallyDeletedNoteID(noteID))
        } else {
            deleteLocallyNoteID(noteID)
        }
    }

    fun observeNoteById(noteID: String) = noteDao.observeNoteById(noteID)

    suspend fun deleteLocallyNoteID(deletedNoteID: String) {
        noteDao.deleteLocallyDeletedNoteID(deletedNoteID)
    }

    suspend fun getNoteById(noteID: String) = noteDao.getNoteById(noteID)

    private var curNotesResponse: Response<List<Note>>? = null

    suspend fun syncNotes() {
        val locallyDeletedNoteID = noteDao.getAllLocallyDeletedNoteIDs()
        locallyDeletedNoteID.forEach { id -> deleteNote(id.deletedNoteID) }

        val unsyncNotes = noteDao.getAllUnsyncedNotes()
        unsyncNotes.forEach { note -> insertNote(note) }

        curNotesResponse = noteApi.getNotes()
        curNotesResponse?.body()?.let { notes ->
            noteDao.deleteAllNotes()
            insertNotes(notes.onEach { note -> note.isSynced = true })
        }
    }


    fun getAllNotes(): Flow<Resource<List<Note>>> {
        return networkBoundResource(
            query = {
                noteDao.getAllNotes()
            },
            fetch = {
                //noteApi.getNotes()
                syncNotes()
                curNotesResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let {
                    insertNotes(it.onEach { note -> note.isSynced = true })
                }
            },
            shouldFetch = {
                checkForInternetConnection(context)
            }
        )
    }

    suspend fun addOwnerToNote(owner: String, noteID: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.addOwnerToNote(AddOwnerRequest(owner, noteID))
            if(response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch(e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.login(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch(e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch(e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }
}