package com.personal.biji.android

import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.RichTextRun
import com.personal.biji.android.editor.EditorBlock
import com.personal.biji.android.editor.blocksFromDocument
import com.personal.biji.android.editor.contentBlocksFromEditorBlocks
import com.personal.biji.android.editor.documentFromEditorBlocks
import com.personal.biji.android.editor.editorTextBlockHeightDp
import com.personal.biji.android.editor.insertImageBlock
import com.personal.biji.android.editor.textFromEditorBlocks
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorBlockTest {
    @Test
    fun blocksFromDocumentKeepsTextAndImageOrder() {
        val image = NoteContentBlock(type = "image", url = "file:///note.jpg")
        val document = RichTextDocument(
            blocks = listOf(
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("第一行\n第二行"))),
                RichTextBlock(type = "image", image = image),
                RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("图后文字"))),
            ),
        )

        val blocks = blocksFromDocument(document, fallbackText = "", fallbackImages = emptyList())

        assertEquals("text", blocks[0].type)
        assertEquals("第一行\n第二行", blocks[0].text)
        assertEquals("image", blocks[1].type)
        assertEquals(image, blocks[1].image)
        assertEquals("图后文字", blocks[2].text)
    }

    @Test
    fun editorBlocksSaveBackToCurrentModels() {
        val image = NoteContentBlock(type = "image", url = "file:///note.jpg")
        val blocks = listOf(
            EditorBlock.Text(id = "t1", text = "第一行\n第二行"),
            EditorBlock.Image(id = "i1", image = image),
            EditorBlock.Text(id = "t2", text = "图后文字"),
        )

        assertEquals("第一行\n第二行\n图后文字", textFromEditorBlocks(blocks))
        assertEquals(listOf(image), contentBlocksFromEditorBlocks(blocks))
        assertEquals(
            RichTextDocument(
                blocks = listOf(
                    RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("第一行\n第二行"))),
                    RichTextBlock(type = "image", image = image),
                    RichTextBlock(type = "paragraph", runs = listOf(RichTextRun("图后文字"))),
                ),
            ),
            documentFromEditorBlocks(blocks),
        )
    }

    @Test
    fun textBlockHeightShrinksWhenImagesAreInTheEditorFlow() {
        assertEquals(44, editorTextBlockHeightDp("", hasMediaBlocks = true))
        assertEquals(44, editorTextBlockHeightDp("短文字", hasMediaBlocks = true))
        assertEquals(88, editorTextBlockHeightDp("第一行\n第二行\n第三行", hasMediaBlocks = true))
        assertEquals(160, editorTextBlockHeightDp("短文字", hasMediaBlocks = false))
    }

    @Test
    fun insertingImageAppendsEditableTextBlockAfterImage() {
        val image = NoteContentBlock(type = "image", url = "file:///note.jpg")
        val blocks = insertImageBlock(listOf(EditorBlock.Text(id = "t1", text = "图前文字")), image)

        assertEquals("text", blocks[0].type)
        assertEquals("image", blocks[1].type)
        assertEquals("text", blocks[2].type)
        assertEquals("", blocks[2].text)
    }

    @Test
    fun insertingImageDoesNotAppendDuplicateEmptyTextBlock() {
        val existingEmpty = EditorBlock.Text(id = "empty", text = "")
        val image = NoteContentBlock(type = "image", url = "file:///note.jpg")
        val blocks = insertImageBlock(listOf(EditorBlock.Text(id = "t1", text = "图前文字"), EditorBlock.Image(id = "i1", image = image), existingEmpty), image)

        assertEquals(4, blocks.size)
        assertEquals("image", blocks[2].type)
        assertEquals("text", blocks[3].type)
        assertEquals("", blocks[3].text)
    }
}
