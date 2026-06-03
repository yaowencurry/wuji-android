package com.personal.biji.android

import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.LibraryExport
import com.personal.biji.android.domain.LibraryLogic
import com.personal.biji.android.domain.NoteTag
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.RichTextMark
import com.personal.biji.android.domain.RichTextRun
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.SYSTEM_QUOTE_TAG_NAME
import com.personal.biji.android.domain.SYSTEM_THOUGHT_TAG_NAME
import com.personal.biji.android.domain.Tag
import com.personal.biji.android.domain.bookNoteStats
import com.personal.biji.android.domain.tagsByNote
import com.personal.biji.android.editor.GsonRichTextCodec
import com.personal.biji.android.editor.summary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DomainLogicTest {
    @Test
    fun manualBookTitlesAreWrappedOnce() {
        assertEquals("《红楼梦》", LibraryLogic.normalizeManualBookTitle(" 红楼梦 "))
        assertEquals("《红楼梦》", LibraryLogic.normalizeManualBookTitle(" 《红楼梦》 "))
    }

    @Test
    fun searchFoldsCaseWidthAndDiacritics() {
        assertTrue(LibraryLogic.matches("Ｃafé METHOD", "cafe method"))
    }

    @Test
    fun markdownExportIncludesBookNoteAndTags() {
        val book = Book(id = "book", title = "沉思录", author = "马可奥勒留")
        val note = Note(id = "note", bookId = book.id, content = "把注意力放在能控制的事情上。", createdAt = Instant.parse("2026-05-28T05:16:42Z"))
        val markdown = LibraryExport.markdown(listOf(book), listOf(note), mapOf(note.id to listOf("哲学")))
        assertTrue(markdown.contains("# 沉思录"))
        assertTrue(markdown.contains("作者：马可奥勒留"))
        assertTrue(markdown.contains("标签：哲学"))
    }

    @Test
    fun bookStatsCountSystemTagsAndLastRecordedTime() {
        val quote = Tag(id = "quote", name = SYSTEM_QUOTE_TAG_NAME)
        val thought = Tag(id = "thought", name = SYSTEM_THOUGHT_TAG_NAME)
        val book = Book(id = "book", title = "置身事内")
        val quoteNote = Note(id = "n1", bookId = book.id, content = "原文", updatedAt = Instant.parse("2026-06-01T09:00:00Z"))
        val thoughtNote = Note(id = "n2", bookId = book.id, content = "想法", updatedAt = Instant.parse("2026-06-02T09:00:00Z"))

        val stats = bookNoteStats(
            book,
            listOf(quoteNote, thoughtNote),
            listOf(quote, thought),
            listOf(NoteTag(noteId = quoteNote.id, tagId = quote.id), NoteTag(noteId = thoughtNote.id, tagId = thought.id)),
        )

        assertEquals(2, stats.totalCount)
        assertEquals(1, stats.quoteCount)
        assertEquals(1, stats.thoughtCount)
        assertEquals(Instant.parse("2026-06-02T09:00:00Z"), stats.lastRecordedAt)
    }

    @Test
    fun markdownExportForBookOnlyIncludesSelectedBookAndStats() {
        val target = Book(id = "target", title = "置身事内", author = "兰小欢")
        val other = Book(id = "other", title = "沉思录")
        val quote = Tag(id = "quote", name = SYSTEM_QUOTE_TAG_NAME)
        val thought = Tag(id = "thought", name = SYSTEM_THOUGHT_TAG_NAME)
        val targetNote = Note(id = "n1", bookId = target.id, content = "财政和土地。", createdAt = Instant.parse("2026-06-01T09:00:00Z"), updatedAt = Instant.parse("2026-06-01T09:00:00Z"))
        val otherNote = Note(id = "n2", bookId = other.id, content = "不要导出。", createdAt = Instant.parse("2026-06-02T09:00:00Z"))
        val noteTags = listOf(NoteTag(noteId = targetNote.id, tagId = quote.id), NoteTag(noteId = targetNote.id, tagId = thought.id))
        val markdown = LibraryExport.markdownForBook(target, listOf(targetNote, otherNote), tagsByNote(listOf(quote, thought), noteTags))

        assertTrue(markdown.contains("# 置身事内"))
        assertTrue(markdown.contains("作者：兰小欢"))
        assertTrue(markdown.contains("摘抄：1"))
        assertTrue(markdown.contains("想法：1"))
        assertTrue(markdown.contains("全部笔记：1"))
        assertTrue(markdown.contains("标签：原文摘抄、我的想法"))
        assertTrue(markdown.contains("财政和土地。"))
        assertTrue(!markdown.contains("不要导出。"))
    }

    @Test
    fun richTextDocumentRoundTripsMarks() {
        val document = RichTextDocument(
            blocks = listOf(RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("重点", listOf(RichTextMark("bold"))))))
        )
        val codec = GsonRichTextCodec()
        assertEquals(document, codec.decode(codec.encode(document)))
    }

    @Test
    fun richTextSummaryDoesNotExposeImagePlaceholder() {
        val document = RichTextDocument(
            blocks = listOf(
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("图前文字"))),
                RichTextBlock(type = "image", image = NoteContentBlock(type = "image", url = "file:///note.jpg")),
            ),
        )

        assertEquals("图前文字", summary(document))
    }

    @Test
    fun richTextSummaryPreservesUserLineBreaksAroundImages() {
        val document = RichTextDocument(
            blocks = listOf(
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("第一行\n第二行\n"))),
                RichTextBlock(type = "image", image = NoteContentBlock(type = "image", url = "file:///note.jpg")),
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("图后文字"))),
            ),
        )

        assertEquals("第一行\n第二行\n\n图后文字", summary(document))
    }
}
