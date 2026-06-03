package com.personal.biji.android.data

import com.google.gson.Gson
import com.personal.biji.android.data.local.BookEntity
import com.personal.biji.android.data.local.LibraryDao
import com.personal.biji.android.data.local.NoteEntity
import com.personal.biji.android.data.local.NoteTagEntity
import com.personal.biji.android.data.local.TagEntity
import com.personal.biji.android.data.remote.BijiApi
import com.personal.biji.android.data.remote.CreateBookRequest
import com.personal.biji.android.data.remote.CreateNoteRequest
import com.personal.biji.android.data.remote.CreateTagRequest
import com.personal.biji.android.data.remote.ReplaceTagsRequest
import com.personal.biji.android.data.remote.UpdateNoteRequest
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.LibraryExport
import com.personal.biji.android.domain.LibraryLogic
import com.personal.biji.android.domain.LibraryRepository
import com.personal.biji.android.domain.LibraryState
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteTag
import com.personal.biji.android.domain.StorageMode
import com.personal.biji.android.domain.SYSTEM_TAG_NAMES
import com.personal.biji.android.domain.Tag
import com.personal.biji.android.domain.isSystemTagName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRepositoryImpl(
    private val dao: LibraryDao,
    private val api: BijiApi?,
    private val gson: Gson,
    private val storageModeStore: StorageModeStore = InMemoryStorageModeStore(StorageMode.Local),
) : LibraryRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(LibraryState())
    override val state: StateFlow<LibraryState> = mutableState.asStateFlow()
    override val storageMode: StateFlow<StorageMode> = storageModeStore.mode

    init {
        scope.launch { ensureSystemTagsForCurrentMode() }
        scope.launch {
            combine(
                storageModeStore.mode.flatMapLatest { dao.observeBooks(it.name) },
                storageModeStore.mode.flatMapLatest { dao.observeNotes(it.name) },
                storageModeStore.mode.flatMapLatest { dao.observeTags(it.name) },
                storageModeStore.mode.flatMapLatest { dao.observeNoteTags(it.name) },
                storageModeStore.mode,
            ) { books, notes, tags, noteTags, mode ->
                LibraryState(
                    books.map { gson.fromJson(it.json, Book::class.java) }.filter { it.deletedAt == null },
                    notes.map { gson.fromJson(it.json, Note::class.java) }.filter { it.deletedAt == null },
                    tags.map { gson.fromJson(it.json, Tag::class.java) }.filter { it.deletedAt == null },
                    noteTags.filter { it.deletedAt == null }.map { NoteTag(noteId = it.noteId, tagId = it.tagId) },
                    mode,
                    mutableState.value.toast,
                )
            }.collect { mutableState.value = it }
        }
    }

    override suspend fun setStorageMode(mode: StorageMode) {
        storageModeStore.setMode(mode)
        ensureSystemTagsForCurrentMode()
        if (mode == StorageMode.Remote) refresh()
    }

    override suspend fun refresh() {
        if (currentMode() == StorageMode.Local) {
            ensureSystemTagsForCurrentMode()
            return
        }
        val remote = api ?: return
        runCatching {
            val books = remote.books().items
            val notes = remote.notes().items
            val tags = remote.tags().items
            dao.clearNoteTags(currentModeName())
            dao.clearBooks(currentModeName())
            dao.clearNotes(currentModeName())
            dao.clearTags(currentModeName())
            dao.upsertBooks(books.map(::bookEntity))
            dao.upsertNotes(notes.map(::noteEntity))
            dao.upsertTags(tags.map(::tagEntity))
            ensureSystemTagsForCurrentMode()
        }
    }

    override suspend fun createBook(title: String, author: String?): Book {
        val normalized = LibraryLogic.normalizeManualBookTitle(title)
        require(normalized.isNotBlank()) { "书名不能为空" }
        val local = Book(title = normalized, author = author?.trim()?.ifBlank { null })
        val value = when (currentMode()) {
            StorageMode.Local -> local
            StorageMode.Remote -> requireApi().createBook(CreateBookRequest(local.title, local.author))
        }
        dao.upsertBooks(listOf(bookEntity(value)))
        return value
    }

    override suspend fun createTag(name: String): Tag {
        val normalized = name.trim()
        require(normalized.isNotBlank()) { "标签名不能为空" }
        require(state.value.tags.none { it.name == normalized }) { "标签已存在" }
        val local = Tag(name = normalized)
        val value = when (currentMode()) {
            StorageMode.Local -> local
            StorageMode.Remote -> requireApi().createTag(CreateTagRequest(normalized))
        }
        dao.upsertTags(listOf(tagEntity(value)))
        return value
    }

    override suspend fun saveNote(note: Note, tagIds: List<String>): Note {
        require(note.content.trim().isNotEmpty()) { "正文不能为空" }
        val existing = dao.notes(currentModeName()).firstOrNull { it.id == note.id && it.deletedAt == null }
        val value = when (currentMode()) {
            StorageMode.Local -> {
                if (existing == null) note.copy(content = note.content.trim())
                else note.copy(content = note.content.trim(), updatedAt = Instant.now(), version = note.version + 1)
            }
            StorageMode.Remote -> {
                val remote = requireApi()
                val saved = if (existing == null) {
                    remote.createNote(CreateNoteRequest(note.bookId, note.content.trim(), note.contentBlocks, note.richTextDocument, note.isFavorite, note.isArchived, tagIds))
                } else {
                    remote.updateNote(note.id, UpdateNoteRequest(note.content.trim(), note.contentBlocks, note.richTextDocument, note.isFavorite, note.isArchived))
                }
                remote.replaceTags(saved.id, ReplaceTagsRequest(tagIds))
                saved
            }
        }
        dao.upsertNotes(listOf(noteEntity(value)))
        dao.upsertNoteTags(tagIds.map { NoteTagEntity(value.id, it, currentModeName()) })
        return value
    }

    override suspend fun deleteBook(id: String) = softDeleteWithRollback(
        apply = {
            state.value.notes.filter { it.bookId == id }.forEach { dao.upsertNotes(listOf(noteEntity(it.copy(deletedAt = Instant.now())))) }
            state.value.books.firstOrNull { it.id == id }?.let { dao.upsertBooks(listOf(bookEntity(it.copy(deletedAt = Instant.now())))) }
        },
        remote = { if (currentMode() == StorageMode.Remote) requireApi().deleteBook(id) },
    )
    override suspend fun deleteNote(id: String) = softDeleteWithRollback(
        apply = { state.value.notes.firstOrNull { it.id == id }?.let { dao.upsertNotes(listOf(noteEntity(it.copy(deletedAt = Instant.now())))) } },
        remote = { if (currentMode() == StorageMode.Remote) requireApi().deleteNote(id) },
    )
    override suspend fun deleteTag(id: String) = softDeleteWithRollback(
        apply = {
            val tag = state.value.tags.firstOrNull { it.id == id } ?: return@softDeleteWithRollback
            require(!isSystemTagName(tag.name)) { "系统标签不能删除" }
            dao.upsertTags(listOf(tagEntity(tag.copy(deletedAt = Instant.now()))))
        },
        remote = { if (currentMode() == StorageMode.Remote) requireApi().deleteTag(id) },
    )

    private suspend fun softDeleteWithRollback(apply: suspend () -> Unit, remote: suspend () -> Unit?) {
        val modeName = currentModeName()
        val books = dao.books(modeName)
        val notes = dao.notes(modeName)
        val tags = dao.tags(modeName)
        val noteTags = dao.noteTags(modeName)
        apply()
        if (currentMode() == StorageMode.Remote && runCatching { remote() }.isFailure) {
            dao.clearBooks(modeName); dao.clearNotes(modeName); dao.clearTags(modeName); dao.clearNoteTags(modeName)
            dao.upsertBooks(books); dao.upsertNotes(notes); dao.upsertTags(tags); dao.upsertNoteTags(noteTags)
            mutableState.value = mutableState.value.copy(toast = "删除失败，已恢复")
        }
    }

    override suspend fun clearLocal() {
        dao.clearNoteTags(currentModeName()); dao.clearNotes(currentModeName()); dao.clearBooks(currentModeName()); dao.clearTags(currentModeName())
    }

    override fun search(query: String): List<Note> {
        if (query.isBlank()) return emptyList()
        val snapshot = state.value
        return snapshot.notes.filter { note ->
            val book = snapshot.books.firstOrNull { it.id == note.bookId }
            val tags = snapshot.noteTags.filter { it.noteId == note.id }.mapNotNull { link -> snapshot.tags.firstOrNull { it.id == link.tagId }?.name }
            LibraryLogic.matches(listOf(note.content, book?.title, book?.author, tags.joinToString(" ")).joinToString(" "), query)
        }
    }

    override fun markdown(): String = LibraryExport.markdown(state.value.books, state.value.notes, tagsByNote())
    override fun jsonBackup(): String = gson.toJson(state.value)
    private fun tagsByNote() = state.value.notes.associate { note ->
        note.id to state.value.noteTags.filter { it.noteId == note.id }.mapNotNull { link -> state.value.tags.firstOrNull { it.id == link.tagId }?.name }
    }
    private fun currentMode() = storageModeStore.mode.value
    private fun currentModeName() = currentMode().name
    private fun requireApi() = requireNotNull(api) { "远端模式需要可用的服务器配置" }
    private suspend fun ensureSystemTagsForCurrentMode() {
        val existing = dao.tags(currentModeName())
            .map { gson.fromJson(it.json, Tag::class.java) }
            .filter { it.deletedAt == null }
        val missing = SYSTEM_TAG_NAMES.filter { name -> existing.none { it.name == name } }
        if (missing.isNotEmpty()) dao.upsertTags(missing.map { name -> tagEntity(Tag(name = name)) })
    }
    private fun bookEntity(value: Book) = BookEntity(value.id, currentModeName(), gson.toJson(value), value.deletedAt?.toString())
    private fun noteEntity(value: Note) = NoteEntity(value.id, currentModeName(), value.bookId, gson.toJson(value), value.deletedAt?.toString())
    private fun tagEntity(value: Tag) = TagEntity(value.id, currentModeName(), gson.toJson(value), value.deletedAt?.toString())
}
