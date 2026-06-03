package com.personal.biji.android.domain

import java.text.Normalizer
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val SYSTEM_QUOTE_TAG_NAME = "原文摘抄"
const val SYSTEM_THOUGHT_TAG_NAME = "我的想法"
val SYSTEM_TAG_NAMES = listOf(SYSTEM_QUOTE_TAG_NAME, SYSTEM_THOUGHT_TAG_NAME)

data class BookNoteStats(
    val totalCount: Int,
    val quoteCount: Int,
    val thoughtCount: Int,
    val lastRecordedAt: java.time.Instant?,
)

fun isSystemTagName(name: String): Boolean = name in SYSTEM_TAG_NAMES

fun tagsByNote(tags: List<Tag>, noteTags: List<NoteTag>): Map<String, List<String>> =
    noteTags.groupBy { it.noteId }.mapValues { (_, links) ->
        links.mapNotNull { link -> tags.firstOrNull { it.id == link.tagId }?.name }
    }

fun bookNoteStats(book: Book, notes: List<Note>, tags: List<Tag>, noteTags: List<NoteTag>): BookNoteStats {
    val bookNotes = notes.filter { it.bookId == book.id && it.deletedAt == null }
    val tagNamesByNote = tagsByNote(tags, noteTags)
    return BookNoteStats(
        totalCount = bookNotes.size,
        quoteCount = bookNotes.count { note -> tagNamesByNote[note.id].orEmpty().contains(SYSTEM_QUOTE_TAG_NAME) },
        thoughtCount = bookNotes.count { note -> tagNamesByNote[note.id].orEmpty().contains(SYSTEM_THOUGHT_TAG_NAME) },
        lastRecordedAt = bookNotes.maxOfOrNull { it.updatedAt },
    )
}

object LibraryLogic {
    fun normalizeManualBookTitle(value: String): String {
        val title = value.trim()
        if (title.isEmpty()) return ""
        return if (title.startsWith("《") && title.endsWith("》")) title else "《$title》"
    }

    fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)

    fun matches(haystack: String, query: String): Boolean =
        normalized(haystack).contains(normalized(query.trim()))
}

object LibraryExport {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    fun markdown(books: List<Book>, notes: List<Note>, tagsByNote: Map<String, List<String>>): String =
        books.filter { it.deletedAt == null }.joinToString("\n\n") { book ->
            buildList {
                add("# ${book.title}")
                book.author?.takeIf(String::isNotBlank)?.let { add("\n作者：$it") }
                notes.filter { it.bookId == book.id && it.deletedAt == null }.forEach { note ->
                    add("\n## ${formatter.format(note.createdAt)}")
                    tagsByNote[note.id]?.takeIf(List<String>::isNotEmpty)?.let { add("标签：${it.joinToString("、")}") }
                    add("\n${note.content}")
                }
            }.joinToString("\n")
        }

    fun markdownForBook(book: Book, notes: List<Note>, tagsByNote: Map<String, List<String>>): String {
        val bookNotes = notes.filter { it.bookId == book.id && it.deletedAt == null }.sortedByDescending { it.updatedAt }
        val quoteCount = bookNotes.count { note -> tagsByNote[note.id].orEmpty().contains(SYSTEM_QUOTE_TAG_NAME) }
        val thoughtCount = bookNotes.count { note -> tagsByNote[note.id].orEmpty().contains(SYSTEM_THOUGHT_TAG_NAME) }
        return buildList {
            add("# ${book.title}")
            book.author?.takeIf(String::isNotBlank)?.let { add("\n作者：$it") }
            add("\n摘抄：$quoteCount")
            add("想法：$thoughtCount")
            add("全部笔记：${bookNotes.size}")
            bookNotes.maxOfOrNull { it.updatedAt }?.let { add("最后记录：${formatter.format(it)}") }
            bookNotes.forEach { note ->
                add("\n## ${formatter.format(note.createdAt)}")
                tagsByNote[note.id]?.takeIf(List<String>::isNotEmpty)?.let { add("标签：${it.joinToString("、")}") }
                add("\n${note.content}")
            }
        }.joinToString("\n")
    }
}
