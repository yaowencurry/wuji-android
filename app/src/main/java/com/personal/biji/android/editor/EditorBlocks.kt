package com.personal.biji.android.editor

import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.RichTextRun
import java.util.UUID
import kotlin.math.ceil

sealed class EditorBlock(
    open val id: String,
    open val type: String,
    open val text: String? = null,
    open val image: NoteContentBlock? = null,
) {
    data class Text(override val id: String = UUID.randomUUID().toString(), override val text: String) : EditorBlock(id, "text", text)
    data class Image(override val id: String = UUID.randomUUID().toString(), override val image: NoteContentBlock) : EditorBlock(id, "image", image = image)
}

fun blocksFromDocument(
    document: RichTextDocument?,
    fallbackText: String,
    fallbackImages: List<NoteContentBlock>,
): List<EditorBlock> {
    val documentBlocks = document?.blocks.orEmpty().mapNotNull { block ->
        block.image?.let { EditorBlock.Image(image = it) }
            ?: EditorBlock.Text(text = block.runs.joinToString("") { it.text })
    }
    if (documentBlocks.isNotEmpty()) return documentBlocks

    val blocks = mutableListOf<EditorBlock>()
    if (fallbackText.isNotEmpty()) blocks += EditorBlock.Text(text = fallbackText)
    blocks += fallbackImages.filter { it.type == "image" && !it.url.isNullOrBlank() }.map { EditorBlock.Image(image = it) }
    return blocks.ifEmpty { listOf(EditorBlock.Text(text = "")) }
}

fun textFromEditorBlocks(blocks: List<EditorBlock>): String =
    blocks.filterIsInstance<EditorBlock.Text>()
        .map { it.text }
        .filter { it.isNotEmpty() }
        .joinToString("\n")

fun contentBlocksFromEditorBlocks(blocks: List<EditorBlock>): List<NoteContentBlock> =
    blocks.filterIsInstance<EditorBlock.Image>().map { it.image }.distinctBy { it.url }

fun documentFromEditorBlocks(blocks: List<EditorBlock>): RichTextDocument = RichTextDocument(
    blocks = blocks.mapNotNull { block ->
        when (block) {
            is EditorBlock.Text -> RichTextBlock(type = "paragraph", runs = listOf(RichTextRun(block.text)))
            is EditorBlock.Image -> RichTextBlock(type = "image", image = block.image)
        }
    },
)

fun insertImageBlock(blocks: List<EditorBlock>, image: NoteContentBlock): List<EditorBlock> {
    val trailingEmptyText = blocks.lastOrNull() as? EditorBlock.Text
    return if (trailingEmptyText != null && trailingEmptyText.text.isBlank()) {
        blocks.dropLast(1) + EditorBlock.Image(image = image) + trailingEmptyText
    } else {
        blocks + EditorBlock.Image(image = image) + EditorBlock.Text(text = "")
    }
}

fun editorTextBlockHeightDp(text: String, hasMediaBlocks: Boolean): Int {
    val visualLines = text.lineSequence().sumOf { line ->
        ceil(line.length.coerceAtLeast(1) / 22f).toInt().coerceAtLeast(1)
    }.coerceAtLeast(1)
    val desired = if (hasMediaBlocks) 22 + visualLines * 22 else 44 + visualLines * 28
    val minHeight = if (hasMediaBlocks) 44 else 160
    val maxHeight = if (hasMediaBlocks) 240 else 360
    return desired.coerceIn(minHeight, maxHeight)
}
