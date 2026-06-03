package com.personal.biji.android.domain

import kotlinx.coroutines.flow.StateFlow

enum class StorageMode(val title: String) {
    Local("本地存储"),
    Remote("远端模式"),
}

data class LibraryState(
    val books: List<Book> = emptyList(),
    val notes: List<Note> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val noteTags: List<NoteTag> = emptyList(),
    val storageMode: StorageMode = StorageMode.Local,
    val toast: String? = null,
)

interface LibraryRepository {
    val state: StateFlow<LibraryState>
    val storageMode: StateFlow<StorageMode>
    suspend fun setStorageMode(mode: StorageMode)
    suspend fun refresh()
    suspend fun createBook(title: String, author: String? = null): Book
    suspend fun createTag(name: String): Tag
    suspend fun saveNote(note: Note, tagIds: List<String>): Note
    suspend fun deleteBook(id: String)
    suspend fun deleteNote(id: String)
    suspend fun deleteTag(id: String)
    suspend fun clearLocal()
    fun search(query: String): List<Note>
    fun markdown(): String
    fun jsonBackup(): String
}
