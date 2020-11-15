package com.synapseslab.ktornotesandroidapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.synapseslab.ktornotesandroidapp.data.local.entities.LocallyDeletedNoteID
import com.synapseslab.ktornotesandroidapp.data.local.entities.Note

@Database(
    entities = [Note::class, LocallyDeletedNoteID::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}