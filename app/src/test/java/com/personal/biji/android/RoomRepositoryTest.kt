package com.personal.biji.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.personal.biji.android.data.LibraryRepositoryImpl
import com.personal.biji.android.data.InMemoryStorageModeStore
import com.personal.biji.android.data.local.BijiDatabase
import com.personal.biji.android.data.remote.ApiFactory
import com.personal.biji.android.data.remote.BijiApi
import com.personal.biji.android.data.remote.CreateBookRequest
import com.personal.biji.android.data.remote.CreateNoteRequest
import com.personal.biji.android.data.remote.CreateTagRequest
import com.personal.biji.android.data.remote.DirectUploadInitRequest
import com.personal.biji.android.data.remote.ListResponse
import com.personal.biji.android.data.remote.ReplaceTagsRequest
import com.personal.biji.android.data.remote.UpdateNoteRequest
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.DirectImageUploadSession
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.RichTextRun
import com.personal.biji.android.domain.StorageMode
import com.personal.biji.android.domain.SYSTEM_QUOTE_TAG_NAME
import com.personal.biji.android.domain.SYSTEM_THOUGHT_TAG_NAME
import com.personal.biji.android.domain.Tag
import com.personal.biji.android.domain.TosTemporaryCredentials
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomRepositoryTest {
    @Test
    fun createBookNoteTagAndSearchPersistLocally() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val repository = LibraryRepositoryImpl(database.libraryDao(), null, ApiFactory.gson)

        val book = repository.createBook("红楼梦")
        val tag = repository.createTag("诗词")
        repository.saveNote(com.personal.biji.android.domain.Note(bookId = book.id, content = "寒塘渡鹤影"), listOf(tag.id))

        kotlinx.coroutines.delay(100)
        assertEquals("《红楼梦》", repository.state.value.books.single().title)
        assertEquals("寒塘渡鹤影", repository.search("诗词").single().content)
        database.close()
    }

    @Test
    fun localModeWritesOnlyToLocalStorageWithoutCallingApi() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val api = RecordingApi()
        val repository = LibraryRepositoryImpl(database.libraryDao(), api, ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Local))

        val book = repository.createBook("红楼梦")
        repository.saveNote(Note(bookId = book.id, content = "只存在本机"), emptyList())

        kotlinx.coroutines.delay(100)
        assertEquals(0, api.createBookCalls)
        assertEquals(0, api.createNoteCalls)
        assertEquals("只存在本机", repository.state.value.notes.single().content)
        database.close()
    }

    @Test
    fun remoteModeRequiresRemoteWriteBeforeCaching() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val api = RecordingApi(failCreates = true)
        val repository = LibraryRepositoryImpl(database.libraryDao(), api, ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Remote))

        runCatching { repository.createBook("红楼梦") }

        kotlinx.coroutines.delay(100)
        assertEquals(1, api.createBookCalls)
        assertTrue(repository.state.value.books.isEmpty())
        database.close()
    }

    @Test
    fun localModePersistsMixedTextAndImageBlocks() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val repository = LibraryRepositoryImpl(database.libraryDao(), RecordingApi(), ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Local))

        val book = repository.createBook("红楼梦")
        val image = NoteContentBlock(type = "image", url = "file:///data/user/0/com.personal.biji.android/files/note-images/local.jpg", objectKey = "local.jpg", altText = "批注配图")
        val document = RichTextDocument(
            blocks = listOf(
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("文字批注"))),
                RichTextBlock(type = "image", image = image),
            ),
        )

        repository.saveNote(Note(bookId = book.id, content = "文字批注", contentBlocks = listOf(image), richTextDocument = document), emptyList())

        kotlinx.coroutines.delay(100)
        val saved = repository.state.value.notes.single()
        assertEquals("文字批注", saved.content)
        assertEquals("file:///data/user/0/com.personal.biji.android/files/note-images/local.jpg", saved.contentBlocks.single().url)
        assertEquals("image", saved.richTextDocument!!.blocks[1].type)
        database.close()
    }

    @Test
    fun savingSameDraftTwiceUpdatesOneNoteInsteadOfDuplicating() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val repository = LibraryRepositoryImpl(database.libraryDao(), RecordingApi(), ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Local))

        val book = repository.createBook("红楼梦")
        val draft = Note(id = "stable-draft", bookId = book.id, content = "第一次")
        repository.saveNote(draft, emptyList())
        repository.saveNote(draft.copy(content = "第二次"), emptyList())

        kotlinx.coroutines.delay(100)
        assertEquals(1, repository.state.value.notes.size)
        assertEquals("stable-draft", repository.state.value.notes.single().id)
        assertEquals("第二次", repository.state.value.notes.single().content)
        database.close()
    }

    @Test
    fun remoteModeUpdatesSavedDraftInsteadOfCreatingSecondNote() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val api = RecordingApi()
        val repository = LibraryRepositoryImpl(database.libraryDao(), api, ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Remote))

        val book = repository.createBook("红楼梦")
        val firstSave = repository.saveNote(Note(id = "local-draft", bookId = book.id, content = "自动保存"), emptyList())
        repository.saveNote(firstSave.copy(content = "手动保存"), emptyList())

        kotlinx.coroutines.delay(100)
        assertEquals(1, api.createNoteCalls)
        assertEquals(1, api.updateNoteCalls)
        assertEquals(1, repository.state.value.notes.size)
        assertEquals("手动保存", repository.state.value.notes.single().content)
        database.close()
    }

    @Test
    fun repositoryEnsuresBuiltInTagsAndRejectsDeletingThem() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, BijiDatabase::class.java).allowMainThreadQueries().build()
        val repository = LibraryRepositoryImpl(database.libraryDao(), RecordingApi(), ApiFactory.gson, InMemoryStorageModeStore(StorageMode.Local))

        kotlinx.coroutines.delay(100)
        val systemTags = repository.state.value.tags.map { it.name }
        assertTrue(SYSTEM_QUOTE_TAG_NAME in systemTags)
        assertTrue(SYSTEM_THOUGHT_TAG_NAME in systemTags)

        val quoteTag = repository.state.value.tags.first { it.name == SYSTEM_QUOTE_TAG_NAME }
        val result = runCatching { repository.deleteTag(quoteTag.id) }

        kotlinx.coroutines.delay(100)
        assertTrue(result.isFailure)
        assertTrue(repository.state.value.tags.any { it.name == SYSTEM_QUOTE_TAG_NAME })
        database.close()
    }

    private class RecordingApi(private val failCreates: Boolean = false) : BijiApi {
        var createBookCalls = 0
        var createNoteCalls = 0
        var updateNoteCalls = 0
        private val books = mutableListOf<Book>()
        private val notes = mutableListOf<Note>()
        private val tags = mutableListOf<Tag>()

        override suspend fun health() = Unit
        override suspend fun books() = ListResponse(books)
        override suspend fun notes() = ListResponse(notes)
        override suspend fun tags() = ListResponse(tags)
        override suspend fun createBook(request: CreateBookRequest): Book {
            createBookCalls += 1
            if (failCreates) error("remote unavailable")
            return Book(title = request.title, author = request.author).also { books += it }
        }
        override suspend fun createNote(request: CreateNoteRequest): Note {
            createNoteCalls += 1
            if (failCreates) error("remote unavailable")
            return Note(bookId = request.bookId, content = request.content, contentBlocks = request.contentBlocks, richTextDocument = request.richTextDocument).also { notes += it }
        }
        override suspend fun createTag(request: CreateTagRequest): Tag {
            if (failCreates) error("remote unavailable")
            return Tag(name = request.name).also { tags += it }
        }
        override suspend fun updateNote(id: String, request: UpdateNoteRequest): Note {
            updateNoteCalls += 1
            if (failCreates) error("remote unavailable")
            val updated = notes.first { it.id == id }.copy(content = request.content ?: notes.first { it.id == id }.content)
            notes.removeAll { it.id == id }
            notes += updated
            return updated
        }
        override suspend fun deleteBook(id: String) { books.removeAll { it.id == id } }
        override suspend fun deleteNote(id: String) { notes.removeAll { it.id == id } }
        override suspend fun deleteTag(id: String) { tags.removeAll { it.id == id } }
        override suspend fun replaceTags(noteId: String, request: ReplaceTagsRequest) = ListResponse(tags.filter { it.id in request.tagIds })
        override suspend fun initDirectUpload(request: DirectUploadInitRequest) = DirectImageUploadSession(
            bucket = "bucket",
            region = "region",
            endpoint = "tos.example.com",
            objectKey = "images/test.jpg",
            url = "https://cdn.example.com/images/test.jpg",
            expiresAt = "2026-06-02T00:00:00Z",
            credentials = TosTemporaryCredentials("ak", "sk", "token"),
        )
    }
}
