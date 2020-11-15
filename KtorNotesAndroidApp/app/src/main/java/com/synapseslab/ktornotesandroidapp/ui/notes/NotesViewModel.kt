package com.synapseslab.ktornotesandroidapp.ui.notes

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.*
import com.synapseslab.ktornotesandroidapp.data.local.entities.LocallyDeletedNoteID
import com.synapseslab.ktornotesandroidapp.data.local.entities.Note
import com.synapseslab.ktornotesandroidapp.other.Event
import com.synapseslab.ktornotesandroidapp.other.Resource
import com.synapseslab.ktornotesandroidapp.repositories.NoteRepository
import kotlinx.coroutines.launch

class NotesViewModel @ViewModelInject constructor(
    private val repository: NoteRepository
): ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _allNotes = _forceUpdate.switchMap {
        repository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }
    val allNotes: LiveData<Event<Resource<List<Note>>>> = _allNotes

    fun syncAllNotes() = _forceUpdate.postValue(true)

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(noteID: String) = viewModelScope.launch {
        repository.deleteNote(noteID)
    }

    fun deletedLocallyDeletedNoteID(deletedNoteID: String) = viewModelScope.launch {
        repository.deleteLocallyNoteID(deletedNoteID)
    }
}