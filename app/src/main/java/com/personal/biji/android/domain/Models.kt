package com.personal.biji.android.domain

import java.time.Instant
import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val remoteId: String? = null,
    val title: String,
    val author: String? = null,
    val coverURL: String? = null,
    val source: String = "manual",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
    val deletedAt: Instant? = null,
    val version: Int = 1,
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val remoteId: String? = null,
    val bookId: String,
    val kind: String = "note",
    val content: String,
    val contentRTFData: String? = null,
    val contentBlocks: List<NoteContentBlock> = emptyList(),
    val richTextDocument: RichTextDocument? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val source: String = "manual",
    val sourceUniqueKey: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
    val deletedAt: Instant? = null,
    val version: Int = 1,
)

data class NoteContentBlock(
    val type: String,
    val text: String? = null,
    val url: String? = null,
    val objectKey: String? = null,
    val altText: String? = null,
    val width: Double? = null,
    val height: Double? = null,
)

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val remoteId: String? = null,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
    val deletedAt: Instant? = null,
    val version: Int = 1,
)

data class NoteTag(
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val tagId: String,
    val createdAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)

data class RichTextDocument(val version: Int = 1, val blocks: List<RichTextBlock> = emptyList())
data class RichTextBlock(val type: String, val runs: List<RichTextRun> = emptyList(), val image: NoteContentBlock? = null)
data class RichTextRun(val text: String, val marks: List<RichTextMark> = emptyList())
data class RichTextMark(val type: String, val value: String? = null)

data class DirectImageUploadSession(
    val bucket: String,
    val region: String,
    val endpoint: String,
    val objectKey: String,
    val url: String,
    val expiresAt: String,
    val credentials: TosTemporaryCredentials,
)
data class TosTemporaryCredentials(val accessKeyId: String, val secretAccessKey: String, val securityToken: String)
data class ImageUploadResponse(val url: String, val objectKey: String)
