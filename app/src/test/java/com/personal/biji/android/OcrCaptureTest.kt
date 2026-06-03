package com.personal.biji.android

import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.editor.EditorBlock
import com.personal.biji.android.ui.OcrParagraph
import com.personal.biji.android.ui.appendOcrEditorBlocks
import com.personal.biji.android.ui.ocrEditorBlocks
import com.personal.biji.android.ui.ocrParagraphs
import com.personal.biji.android.ui.selectedOcrText
import org.junit.Assert.assertEquals
import org.junit.Test

class OcrCaptureTest {
    @Test
    fun paragraphsTrimWhitespaceFilterBlankAndKeepOrder() {
        assertEquals(
            listOf(
                OcrParagraph(id = "ocr-0", text = "第一段"),
                OcrParagraph(id = "ocr-1", text = "第二段"),
            ),
            ocrParagraphs(listOf("  第一段  ", "", "  ", "第二段")),
        )
    }

    @Test
    fun selectedTextJoinsOnlySelectedParagraphs() {
        assertEquals(
            "第一段\n\n第三段",
            selectedOcrText(
                listOf(
                    OcrParagraph(id = "1", text = "第一段"),
                    OcrParagraph(id = "2", text = "第二段", selected = false),
                    OcrParagraph(id = "3", text = "第三段"),
                ),
            ),
        )
    }

    @Test
    fun ocrEditorBlocksKeepTextBeforeOptionalOriginalImage() {
        val blocks = ocrEditorBlocks(
            text = "摘抄正文",
            image = NoteContentBlock(type = "image", url = "file:///original.jpg"),
        )

        assertEquals("摘抄正文", (blocks[0] as EditorBlock.Text).text)
        assertEquals("file:///original.jpg", (blocks[1] as EditorBlock.Image).image.url)
        assertEquals("", (blocks[2] as EditorBlock.Text).text)
    }

    @Test
    fun appendOcrBlocksKeepsExistingContent() {
        val existing = listOf<EditorBlock>(EditorBlock.Text(id = "old", text = "原有内容"))
        val added = listOf<EditorBlock>(EditorBlock.Text(id = "new", text = "新增摘抄"))

        assertEquals(listOf("原有内容", "新增摘抄"), appendOcrEditorBlocks(existing, added).map { it.text })
    }
}
