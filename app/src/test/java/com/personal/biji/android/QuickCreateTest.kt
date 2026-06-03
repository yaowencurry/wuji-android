package com.personal.biji.android

import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.ui.QuickCreateSeed
import com.personal.biji.android.ui.imageQuickCreateCandidate
import com.personal.biji.android.ui.selectQuickCreateCandidate
import com.personal.biji.android.ui.textQuickCreateCandidate
import com.personal.biji.android.ui.trimQuickCreateHistory
import com.personal.biji.android.ui.quickCreatePreview
import com.personal.biji.android.ui.randomPromptFromNotes
import com.personal.biji.android.ui.seedEditorBlocks
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickCreateTest {
    @Test
    fun randomPromptSkipsEmptyAndImageOnlyNotes() {
        val prompt = randomPromptFromNotes(
            listOf(
                Note(id = "empty", bookId = "book", content = ""),
                Note(id = "image", bookId = "book", content = "[图片]", contentBlocks = listOf(NoteContentBlock(type = "image", url = "file:///a.jpg"))),
                Note(id = "text", bookId = "book", content = "阅读不是把文字带走，而是把某一刻的自己留下。"),
            ),
        )

        assertEquals("text", prompt?.noteId)
        assertEquals("阅读不是把文字带走，而是把某一刻的自己留下。", prompt?.text)
    }

    @Test
    fun randomPromptReturnsNullWithoutTextNotes() {
        assertNull(randomPromptFromNotes(listOf(Note(bookId = "book", content = "[图片]"))))
    }

    @Test
    fun quickCreatePreviewTruncatesLongTextToThirtyChars() {
        val preview = quickCreatePreview(QuickCreateSeed.Text("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十还有更多"))

        assertEquals("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十...", preview)
    }

    @Test
    fun textSeedBuildsInitialEditorBlocks() {
        val blocks = seedEditorBlocks(QuickCreateSeed.Text("剪贴板文字"))

        assertEquals("text", blocks!!.single().type)
        assertEquals("剪贴板文字", blocks.single().text)
    }

    @Test
    fun imageSeedBuildsImageThenEmptyTextBlock() {
        val image = NoteContentBlock(type = "image", url = "file:///screenshot.jpg")
        val blocks = seedEditorBlocks(QuickCreateSeed.Image(image))

        assertEquals("image", blocks!![0].type)
        assertEquals("text", blocks[1].type)
        assertEquals("", blocks[1].text)
    }

    @Test
    fun textCandidateTrimsWhitespaceAndUsesStableFingerprint() {
        val first = textQuickCreateCandidate("  剪贴板文字  ", 1_000)
        val second = textQuickCreateCandidate("剪贴板文字", 2_000)

        assertEquals("剪贴板文字", (first!!.seed as QuickCreateSeed.Text).value)
        assertEquals(first.fingerprint, second!!.fingerprint)
    }

    @Test
    fun imageCandidateFingerprintDistinguishesUriAndTimestamp() {
        val first = imageQuickCreateCandidate("content://images/1", 1_000)
        val otherUri = imageQuickCreateCandidate("content://images/2", 1_000)
        val otherTimestamp = imageQuickCreateCandidate("content://images/1", 2_000)

        assertNotEquals(first.fingerprint, otherUri.fingerprint)
        assertNotEquals(first.fingerprint, otherTimestamp.fingerprint)
    }

    @Test
    fun quickCreateSelectionReturnsLatestUnprocessedRecentCandidate() {
        val oldText = textQuickCreateCandidate("文字", 80_000)!!
        val newImage = imageQuickCreateCandidate("content://images/1", 95_000)

        val selected = selectQuickCreateCandidate(
            listOf(oldText, newImage),
            processedFingerprints = emptySet(),
            nowMillis = 100_000,
        )

        assertEquals(newImage, selected)
    }

    @Test
    fun quickCreateSelectionSkipsProcessedAndExpiredCandidates() {
        val expired = textQuickCreateCandidate("过期", 60_000)!!
        val processed = textQuickCreateCandidate("处理过", 95_000)!!

        val selected = selectQuickCreateCandidate(
            listOf(expired, processed),
            processedFingerprints = setOf(processed.fingerprint),
            nowMillis = 100_000,
        )

        assertNull(selected)
    }

    @Test
    fun clipboardCandidateUsesForegroundScanTimeInsteadOfExternalTimestamp() {
        val candidate = textQuickCreateCandidate("刚复制的文字", timestampMillis = 100_000)!!

        assertEquals(
            candidate,
            selectQuickCreateCandidate(
                candidates = listOf(candidate),
                processedFingerprints = emptySet(),
                nowMillis = 100_000,
            ),
        )
    }

    @Test
    fun quickCreateHistoryKeepsMostRecentOneHundredFingerprints() {
        val history = trimQuickCreateHistory(
            existing = (0 until 100).map { "old-$it" },
            consumed = listOf("new"),
        )

        assertEquals(100, history.size)
        assertEquals("old-1", history.first())
        assertEquals("new", history.last())
    }
}
