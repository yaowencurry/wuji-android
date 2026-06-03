package com.personal.biji.android.data.remote

import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.DirectImageUploadSession
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.Tag
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ListResponse<T>(val items: List<T>)
data class CreateBookRequest(val title: String, val author: String? = null, val coverURL: String? = null, val source: String = "manual")
data class CreateTagRequest(val name: String, val colorHex: String? = null)
data class CreateNoteRequest(
    val bookId: String,
    val content: String,
    val contentBlocks: List<NoteContentBlock> = emptyList(),
    val richTextDocument: RichTextDocument? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val tagIds: List<String> = emptyList(),
)
data class UpdateNoteRequest(
    val content: String? = null,
    val contentBlocks: List<NoteContentBlock>? = null,
    val richTextDocument: RichTextDocument? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean? = null,
)
data class ReplaceTagsRequest(val tagIds: List<String>)
data class DirectUploadInitRequest(val contentType: String, val byteSize: Long, val fileExtension: String)

interface BijiApi {
    @GET("healthz") suspend fun health()
    @GET("v1/books") suspend fun books(): ListResponse<Book>
    @POST("v1/books") suspend fun createBook(@Body request: CreateBookRequest): Book
    @DELETE("v1/books/{id}") suspend fun deleteBook(@Path("id") id: String)
    @GET("v1/notes") suspend fun notes(): ListResponse<Note>
    @POST("v1/notes") suspend fun createNote(@Body request: CreateNoteRequest): Note
    @PATCH("v1/notes/{id}") suspend fun updateNote(@Path("id") id: String, @Body request: UpdateNoteRequest): Note
    @DELETE("v1/notes/{id}") suspend fun deleteNote(@Path("id") id: String)
    @GET("v1/tags") suspend fun tags(): ListResponse<Tag>
    @POST("v1/tags") suspend fun createTag(@Body request: CreateTagRequest): Tag
    @DELETE("v1/tags/{id}") suspend fun deleteTag(@Path("id") id: String)
    @PUT("v1/notes/{id}/tags") suspend fun replaceTags(@Path("id") noteId: String, @Body request: ReplaceTagsRequest): ListResponse<Tag>
    @POST("v1/uploads/images/direct-init") suspend fun initDirectUpload(@Body request: DirectUploadInitRequest): DirectImageUploadSession
}
