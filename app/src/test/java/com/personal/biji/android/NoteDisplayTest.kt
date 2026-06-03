package com.personal.biji.android

import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.ui.noteDisplaySummary
import com.personal.biji.android.ui.noteImages
import com.personal.biji.android.ui.noteTimeText
import com.personal.biji.android.ui.TIMELINE_CARD_MIN_HEIGHT_DP
import com.personal.biji.android.ui.TIMELINE_TIME_COLUMN_MIN_HEIGHT_DP
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class NoteDisplayTest {
    @Test
    fun summaryRemovesImagePlaceholderFromMixedContent() {
        val note = Note(bookId = "book", content = "  [图片]\n真正的批注  ")

        assertEquals("真正的批注", noteDisplaySummary(note))
    }

    @Test
    fun summaryNamesPureImageNoteWithoutShowingPlaceholder() {
        val note = Note(
            bookId = "book",
            content = "[图片]",
            contentBlocks = listOf(NoteContentBlock(type = "image", url = "file:///note.jpg")),
        )

        assertEquals("图片笔记", noteDisplaySummary(note))
    }

    @Test
    fun imageCollectionReadsContentBlocksAndRichTextBlocks() {
        val local = NoteContentBlock(type = "image", url = "file:///local.jpg")
        val remote = NoteContentBlock(type = "image", url = "https://cdn.example.com/remote.jpg")
        val note = Note(
            bookId = "book",
            content = "图文",
            contentBlocks = listOf(local),
            richTextDocument = RichTextDocument(blocks = listOf(RichTextBlock(type = "image", image = remote))),
        )

        assertEquals(listOf(local, remote), noteImages(note))
    }

    @Test
    fun noteTimeTextShowsHoursAndMinutesWithoutSeconds() {
        val value = noteTimeText(
            Instant.parse("2026-06-02T10:46:59Z"),
            ZoneId.of("Asia/Shanghai"),
        )

        assertEquals("18:46", value)
    }

    @Test
    fun timelineCardMinimumHeightAccommodatesTimeColumn() {
        assertTrue(TIMELINE_CARD_MIN_HEIGHT_DP >= TIMELINE_TIME_COLUMN_MIN_HEIGHT_DP)
    }
}
