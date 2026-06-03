package com.personal.biji.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
import com.personal.biji.android.domain.StorageMode
import com.personal.biji.android.domain.Tag
import com.personal.biji.android.domain.TosTemporaryCredentials
import com.personal.biji.android.upload.LocalImageUploader
import com.personal.biji.android.upload.ModeAwareImageUploader
import com.personal.biji.android.upload.ImageUploader
import com.personal.biji.android.domain.ImageUploadResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ImageUploaderTest {
    @Test
    fun localImageUploaderStoresBytesInAppFiles() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val uploader = LocalImageUploader(context)
        val data = byteArrayOf(1, 2, 3, 4)

        val response = uploader.upload(data)

        assertTrue(response.url.startsWith("file://"))
        val saved = File(response.url.removePrefix("file://"))
        assertTrue(saved.exists())
        assertArrayEquals(data, saved.readBytes())
        assertTrue(response.objectKey.startsWith("local/"))
    }

    @Test
    fun modeAwareUploaderUsesLocalOrRemotePath() = runBlocking {
        val mode = MutableStateFlow(StorageMode.Local)
        val remote = RecordingUploader()
        val uploader = ModeAwareImageUploader(mode, LocalImageUploader(ApplicationProvider.getApplicationContext()), remote)

        val local = uploader.upload(byteArrayOf(1))
        mode.value = StorageMode.Remote
        val remoteResponse = uploader.upload(byteArrayOf(2))

        assertTrue(local.url.startsWith("file://"))
        assertEquals("https://cdn.example.com/remote.jpg", remoteResponse.url)
        assertEquals(1, remote.calls)
    }

    private class RecordingUploader : ImageUploader {
        var calls = 0
        override suspend fun upload(data: ByteArray, contentType: String): ImageUploadResponse {
            calls += 1
            return ImageUploadResponse("https://cdn.example.com/remote.jpg", "remote.jpg")
        }
    }

    private class UnusedApi : BijiApi {
        override suspend fun health() = Unit
        override suspend fun books() = ListResponse(emptyList<Book>())
        override suspend fun createBook(request: CreateBookRequest) = error("unused")
        override suspend fun deleteBook(id: String) = Unit
        override suspend fun notes() = ListResponse(emptyList<Note>())
        override suspend fun createNote(request: CreateNoteRequest) = error("unused")
        override suspend fun updateNote(id: String, request: UpdateNoteRequest) = error("unused")
        override suspend fun deleteNote(id: String) = Unit
        override suspend fun tags() = ListResponse(emptyList<Tag>())
        override suspend fun createTag(request: CreateTagRequest) = error("unused")
        override suspend fun deleteTag(id: String) = Unit
        override suspend fun replaceTags(noteId: String, request: ReplaceTagsRequest) = ListResponse(emptyList<Tag>())
        override suspend fun initDirectUpload(request: DirectUploadInitRequest) = DirectImageUploadSession(
            bucket = "bucket",
            region = "region",
            endpoint = "tos.example.com",
            objectKey = "remote.jpg",
            url = "https://cdn.example.com/remote.jpg",
            expiresAt = "2026-06-02T00:00:00Z",
            credentials = TosTemporaryCredentials("ak", "sk", "token"),
        )
    }
}
